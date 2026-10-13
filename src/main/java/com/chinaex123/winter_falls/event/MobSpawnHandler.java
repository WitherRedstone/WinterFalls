package com.chinaex123.winter_falls.event;

import com.chinaex123.winter_falls.Winterfalls;
import com.chinaex123.winter_falls.config.WFConfig;
import com.chinaex123.winter_falls.util.WinterFallsWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.function.Consumer;

/**
 * 生物生成处理器。
 * <p>
 * 负责冬季世界中的自定义生物生成逻辑：
 * 在每个玩家周围周期性尝试生成雪傀儡、流浪者、北极熊与狐狸，
 * 限制各类生物的数量上限，并将自然生成的骷髅转换为流浪者、
 * 将兔子强制设为白色、将狐狸强制设为雪狐。
 */
@EventBusSubscriber(modid = Winterfalls.MOD_ID)
public class MobSpawnHandler {

    /** 生物生成检查间隔（刻） */
    private static final int SPAWN_CHECK_INTERVAL = 40;

    /**
     * 原版自然生成范围：距离玩家 24-128 格的环形区域。
     * 24 格内不生成（避免贴脸），128 格外也不生成（超出活跃范围）。
     */
    private static final int SPAWN_RANGE_MIN = 24;
    private static final int SPAWN_RANGE_MAX = 128;

    /**
     * 原版数量上限
     */
    private static final int CREATURE_SPAWN_CAP = 10;
    private static final int MONSTER_SPAWN_CAP = 50;

    /**
     * 处理世界刻事件。
     * <p>
     * 仅对服务端的冬季世界生效，按固定间隔且在存在玩家时，
     * 为每个玩家独立尝试生成各类生物，并受对应数量上限约束：
     * 雪傀儡与北极熊、狐狸全天生成，流浪者仅在夜晚生成。
     *
     * @param event 世界刻事件
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;
        if (level.getGameTime() % SPAWN_CHECK_INTERVAL != 0) return;
        if (level.players().isEmpty()) return;

        RandomSource random = level.getRandom();

        // 每个玩家周围独立尝试生成
        for (var player : level.players()) {
            BlockPos center = player.blockPosition();

            // 雪傀儡
            if (getNearbyEntityCount(level, center, SnowGolem.class) < CREATURE_SPAWN_CAP) {
                trySpawnSnowGolem(level, random, center);
            }

            // 流浪者
            if (level.isNight()
                    && getNearbyEntityCount(level, center, Stray.class) < MONSTER_SPAWN_CAP) {
                trySpawnStray(level, random, center);
            }

            // 北极熊
            if (getNearbyEntityCount(level, center, PolarBear.class) < CREATURE_SPAWN_CAP) {
                trySpawnPolarBear(level, random, center);
            }

            // 狐狸
            if (getNearbyEntityCount(level, center, Fox.class) < CREATURE_SPAWN_CAP) {
                trySpawnFox(level, random, center);
            }
        }
    }

    /**
     * 处理生物最终生成事件。
     * <p>
     * 仅对服务端的冬季世界生效：
     * 将自然生成的骷髅按配置概率替换为流浪者；
     * 将自然生成的兔子强制设为白色变种。
     *
     * @param event 生物最终生成事件
     */
    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!WinterFallsWorld.isWinterFallsWorld(level)) return;

        // 骷髅：转换成流浪者
        if (event.getEntity() instanceof Skeleton skeleton
                && !(event.getEntity() instanceof Stray)) {
            MobSpawnType spawnType = event.getSpawnType();
            if (spawnType == MobSpawnType.TRIGGERED
                    || spawnType == MobSpawnType.COMMAND
                    || spawnType == MobSpawnType.SPAWNER) {
                return;
            }

            double chance = WFConfig.SKELETON_TO_STRAY_CHANCE.get();
            if (level.getRandom().nextDouble() < chance) {
                replaceSkeletonWithStray(level, skeleton, event);
            }
        }

        // 兔子：自然生成的强制白色
        if (event.getEntity() instanceof Rabbit rabbit) {
            MobSpawnType spawnType = event.getSpawnType();
            if (spawnType != MobSpawnType.TRIGGERED
                    && spawnType != MobSpawnType.COMMAND
                    && spawnType != MobSpawnType.SPAWNER
                    && spawnType != MobSpawnType.DISPENSER) {
                rabbit.setVariant(Rabbit.Variant.WHITE);
            }
        }
    }

    /**
     * 统计玩家周围指定类型生物的数量。
     *
     * @param level     服务端世界
     * @param center    统计中心
     * @param entityCls 生物类型
     * @return 生成范围内的该类生物数量
     */
    private static <T extends Entity> int getNearbyEntityCount(ServerLevel level, BlockPos center, Class<T> entityCls) {
        AABB aabb = new AABB(center).inflate(SPAWN_RANGE_MAX);
        return level.getEntitiesOfClass(entityCls, aabb).size();
    }

    /**
     * 判断该群系是否原生会生成指定生物。
     *
     * @param level    服务端世界
     * @param pos      位置
     * @param category 生物类别
     * @param type     实体类型
     * @return 若该群系原生会生成该生物则返回 true
     */
    private static boolean isNativeSpawnBiome(ServerLevel level, BlockPos pos, MobCategory category, EntityType<?> type) {
        return level.getBiome(pos).value().getMobSettings()
                .getMobs(category)
                .unwrap()
                .stream()
                .anyMatch(data -> data.type == type);
    }

    /**
     * 在 min~max 之间随机取一个数量。
     *
     * @param random   随机源
     * @param minCount 最小数量
     * @param maxCount 最大数量
     * @return 随机得到的数量
     */
    private static int rollCount(RandomSource random, int minCount, int maxCount) {
        return minCount + random.nextInt(Math.max(1, maxCount - minCount + 1));
    }

    /**
     * 通用批量生成方法。
     * <p>
     * 按给定数量反复寻找合适位置并生成指定类型生物，
     * 每个生物生成后可执行自定义逻辑。
     *
     * @param count         生成数量
     * @param maxLightLevel 天空光照阈值
     * @param type          实体类型
     * @param customizer    生成后的自定义逻辑，可为 null
     */
    private static <T extends Mob> void spawnMobs(ServerLevel level, RandomSource random, BlockPos center,
                                                  int count, int maxLightLevel,
                                                  EntityType<T> type,
                                                  Consumer<T> customizer) {
        for (int i = 0; i < count; i++) {
            BlockPos spawnPos = findRandomSpawnPos(level, center, random, maxLightLevel);
            if (spawnPos == null) continue;

            T mob = type.create(level);
            if (mob == null) continue;

            mob.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5,
                    random.nextFloat() * 360.0F, 0.0F);

            if (customizer != null) {
                customizer.accept(mob);
            }

            level.addFreshEntity(mob);
        }
    }

    /**
     * 尝试生成雪傀儡。
     * <p>
     * 按配置的生成概率与数量范围，
     * 在玩家周围随机寻找合适位置生成雪傀儡。
     *
     * @param level  服务端世界
     * @param random 随机源
     * @param center 生成中心
     */
    private static void trySpawnSnowGolem(ServerLevel level, RandomSource random, BlockPos center) {
        double spawnChance = WFConfig.SNOW_GOLEM_SPAWN_CHANCE.get();
        if (random.nextDouble() >= spawnChance) return;

        int count = rollCount(random,
                WFConfig.SNOW_GOLEM_MIN_COUNT.get(),
                WFConfig.SNOW_GOLEM_MAX_COUNT.get());
        if (count <= 0) return;

        spawnMobs(level, random, center, count, 15, EntityType.SNOW_GOLEM, null);
    }

    /**
     * 尝试生成流浪者。
     * <p>
     * 若当前群系原生会生成流浪者则跳过。按配置的生成概率与数量范围，
     * 在较暗的位置生成持有弓的流浪者。
     *
     * @param level  服务端世界
     * @param random 随机源
     * @param center 生成中心
     */
    private static void trySpawnStray(ServerLevel level, RandomSource random, BlockPos center) {
        // 排除原版流浪者原生群系
        if (isNativeSpawnBiome(level, center, MobCategory.MONSTER, EntityType.STRAY)) return;

        double spawnChance = WFConfig.STRAY_SPAWN_CHANCE.get();
        if (random.nextDouble() >= spawnChance) return;

        int count = rollCount(random,
                WFConfig.STRAY_MIN_COUNT.get(),
                WFConfig.STRAY_MAX_COUNT.get());
        if (count <= 0) return;

        spawnMobs(level, random, center, count, 7, EntityType.STRAY, stray ->
                stray.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance()));
    }

    /**
     * 尝试生成北极熊。
     * <p>
     * 若当前群系原生会生成北极熊则跳过。按配置的生成概率与数量范围，
     * 生成约一半成年北极熊与一半小熊。
     *
     * @param level  服务端世界
     * @param random 随机源
     * @param center 生成中心
     */
    private static void trySpawnPolarBear(ServerLevel level, RandomSource random, BlockPos center) {
        // 排除原版北极熊原生群系
        if (isNativeSpawnBiome(level, center, MobCategory.CREATURE, EntityType.POLAR_BEAR)) return;

        double spawnChance = WFConfig.POLAR_BEAR_SPAWN_CHANCE.get();
        if (random.nextDouble() >= spawnChance) return;

        int total = rollCount(random,
                WFConfig.POLAR_BEAR_MIN_COUNT.get(),
                WFConfig.POLAR_BEAR_MAX_COUNT.get());
        if (total <= 0) return;

        // 小熊数 = 总数 / 2，剩下的是大熊
        int cubCount = total / 2;
        int adultCount = total - cubCount;

        spawnMobs(level, random, center, adultCount, 15, EntityType.POLAR_BEAR, null);
        spawnMobs(level, random, center, cubCount, 15, EntityType.POLAR_BEAR, bear -> bear.setBaby(true));
    }

    /**
     * 尝试生成狐狸。
     * <p>
     * 若当前群系原生会生成狐狸则跳过。按配置的生成概率与数量范围生成雪狐，
     * 有概率生成为小狐狸，并有概率让其叼着随机战利品。
     *
     * @param level  服务端世界
     * @param random 随机源
     * @param center 生成中心
     */
    private static void trySpawnFox(ServerLevel level, RandomSource random, BlockPos center) {
        // 排除原版狐狸原生群系
        if (isNativeSpawnBiome(level, center, MobCategory.CREATURE, EntityType.FOX)) return;

        double spawnChance = WFConfig.FOX_SPAWN_CHANCE.get();
        if (random.nextDouble() >= spawnChance) return;

        int count = rollCount(random,
                WFConfig.FOX_MIN_COUNT.get(),
                WFConfig.FOX_MAX_COUNT.get());
        if (count <= 0) return;

        spawnMobs(level, random, center, count, 15, EntityType.FOX, fox -> {
            // 强制设为白色极地狐
            fox.setVariant(Fox.Type.SNOW);

            // 5% 概率生成为小狐狸
            if (!fox.isBaby() && random.nextFloat() < 0.05F) {
                fox.setBaby(true);
            }

            // 先读取最终状态，再决定叼物品概率
            boolean isBaby = fox.isBaby();
            if (random.nextFloat() < (isBaby ? 0.35F : 0.15F)) {
                fox.setItemSlot(EquipmentSlot.MAINHAND, pickFoxLoot(random));
            }
        });
    }

    /**
     * 随机挑选狐狸叼着的战利品。
     * <p>
     * 按顺序依次判定各类物品的概率，均未命中时返回金粒。
     *
     * @param random 随机源
     * @return 选中的战利品物品栈
     */
    private static ItemStack pickFoxLoot(RandomSource random) {
        if (random.nextDouble() < 0.40) return Items.SWEET_BERRIES.getDefaultInstance();
        if (random.nextDouble() < 0.20) return Items.FEATHER.getDefaultInstance();
        if (random.nextDouble() < 0.15) return Items.WHEAT_SEEDS.getDefaultInstance();
        if (random.nextDouble() < 0.10) return Items.RABBIT_FOOT.getDefaultInstance();
        if (random.nextDouble() < 0.05) return Items.RABBIT.getDefaultInstance();
        if (random.nextDouble() < 0.05) return Items.EMERALD.getDefaultInstance();
        return Items.GOLD_NUGGET.getDefaultInstance();
    }

    /**
     * 在玩家周围随机寻找合适的生成位置。
     * <p>
     * 在 24-128 格的环形区域内随机取点，最多尝试 10 次。
     * 要求所在区块已加载、该位置上两格均为空气，
     * 且天空光照不超过给定阈值。
     *
     * @param level         服务端世界
     * @param center        生成中心
     * @param random        随机源
     * @param maxLightLevel 允许的最大天空光照等级
     * @return 合适的生成位置，未找到时返回 null
     */
    private static BlockPos findRandomSpawnPos(ServerLevel level, BlockPos center, RandomSource random,
                                               int maxLightLevel) {
        for (int attempt = 0; attempt < 10; attempt++) {
            // 在环形区域随机取点（24-128 格）
            double angle = random.nextDouble() * Math.PI * 2;
            double radius = SPAWN_RANGE_MIN + random.nextDouble() * (SPAWN_RANGE_MAX - SPAWN_RANGE_MIN);
            int x = center.getX() + (int) (Math.cos(angle) * radius);
            int z = center.getZ() + (int) (Math.sin(angle) * radius);

            // 检查区块是否加载
            if (!level.hasChunk(x >> 4, z >> 4)) continue;

            // 取高度图
            BlockPos candidate = new BlockPos(x, level.getMinBuildHeight(), z);
            candidate = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, candidate);

            // 上方两格必须是空气
            if (!level.getBlockState(candidate.above()).isAir()) continue;
            if (!level.getBlockState(candidate.above(2)).isAir()) continue;

            // 天空光照不超过阈值
            if (level.getBrightness(LightLayer.SKY, candidate) <= maxLightLevel) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * 将原骷髅替换为流浪者。
     * <p>
     * 取消原骷髅的生成，在原地生成一只流浪者，
     * 继承原骷髅的全部装备；若原骷髅没有武器，
     * 则为流浪者配备一把弓，最后移除原骷髅。
     *
     * @param level    服务端世界
     * @param skeleton 原骷髅实体
     * @param event    生物最终生成事件
     */
    private static void replaceSkeletonWithStray(ServerLevel level, Skeleton skeleton, FinalizeSpawnEvent event) {
        event.setSpawnCancelled(true);

        Stray stray = EntityType.STRAY.create(level);
        if (stray == null) return;

        stray.moveTo(skeleton.getX(), skeleton.getY(), skeleton.getZ(),
                skeleton.getYRot(), skeleton.getXRot());

        // 继承装备
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            stray.setItemSlot(slot, skeleton.getItemBySlot(slot));
        }
        if (skeleton.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            stray.setItemSlot(EquipmentSlot.MAINHAND, Items.BOW.getDefaultInstance());
        }

        level.addFreshEntity(stray);
        skeleton.discard();
    }
}