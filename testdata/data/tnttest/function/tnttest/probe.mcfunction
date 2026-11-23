say PROBE_START
execute if block 400 -60 0 minecraft:air run say P_400_60_AIR
execute if block 400 -60 0 compressedtnt:triple_compressed_tnt run say P_400_TNT_STILL
execute if block 401 -60 0 minecraft:redstone_block run say P_401_REDSTONE
execute if block 400 -61 0 minecraft:grass_block run say P_400_61_GRASS
execute if block 435 -60 0 minecraft:stone run say P_435_STONE
execute if block 435 -60 0 minecraft:air run say P_435_AIR
execute if block 438 -60 0 minecraft:stone run say P_438_STONE
execute if entity @e[type=compressedtnt:primed_compressed_tnt] run say P_ENT_PRESENT
execute as @e[type=compressedtnt:primed_compressed_tnt] run data get entity @s Pos
say PROBE_END
