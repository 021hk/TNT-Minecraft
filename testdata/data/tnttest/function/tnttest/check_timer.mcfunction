scoreboard players set chk_fuse tnttest 1
scoreboard players set chk_vfuse tnttest 1
# dump any compressed entity that is still around (diagnostics)
execute as @e[type=compressedtnt:primed_compressed_tnt] run data get entity @s CompressedFuse
execute as @e[type=compressedtnt:primed_compressed_tnt] run data get entity @s Pos
execute as @e[type=compressedtnt:primed_compressed_tnt] run data get entity @s CompressedNukeShell
# compressed TNT summoned with CompressedFuse:40 (2 s) must have exploded on time
execute if entity @e[type=compressedtnt:primed_compressed_tnt] run say DIAG_FUSE_ENTITY_STILL_THERE
execute if entity @e[type=compressedtnt:primed_compressed_tnt] run scoreboard players set chk_fuse tnttest 0
execute unless block 706 -60 0 minecraft:air run say DIAG_706_NOT_DESTROYED
execute unless block 706 -60 0 minecraft:air run scoreboard players set chk_fuse tnttest 0
execute if block 709 -60 0 minecraft:air run say DIAG_709_DESTROYED
execute if block 709 -60 0 minecraft:air run scoreboard players set chk_fuse tnttest 0
# vanilla TNT summoned with Fuse:40 must have exploded on time too
execute if entity @e[type=minecraft:tnt] run scoreboard players set chk_vfuse tnttest 0
execute unless block 731 -61 0 minecraft:air run say DIAG_731_NOT_DESTROYED
execute unless block 731 -61 0 minecraft:air run scoreboard players set chk_vfuse tnttest 0
say CHECK_TIMER_DONE
