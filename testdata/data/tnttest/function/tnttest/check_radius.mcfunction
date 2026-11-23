scoreboard players set chk_radius tnttest 1
# tier 1 (r=8): marker at dx=7 destroyed, marker at dx=9 intact
execute unless block 7 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute unless block 7 -60 0 minecraft:air run say DIAG_T1_7_NOT_DESTROYED
execute if block 9 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 9 -60 0 minecraft:air run say DIAG_T1_9_DESTROYED
# tier 2 (r=16): marker at dx=15 destroyed, marker at dx=18 intact
execute unless block 215 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute unless block 215 -60 0 minecraft:air run say DIAG_T2_215_NOT_DESTROYED
execute if block 218 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 218 -60 0 minecraft:air run say DIAG_T2_218_DESTROYED
# tier 3 (r=24): marker at dx=23 destroyed, marker at dx=26 intact
execute unless block 423 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute unless block 423 -60 0 minecraft:air run say DIAG_T3_423_NOT_DESTROYED
execute if block 426 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 426 -60 0 minecraft:air run say DIAG_T3_426_DESTROYED
