scoreboard players set chk_radius tnttest 1
# tier 1 (r=8): marker at dx=7 destroyed, marker at dx=9 intact
execute unless block 7 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 9 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
# tier 2 (r=17): marker at dx=15 destroyed, marker at dx=18 intact
execute unless block 215 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 218 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
# tier 3 (r=36): marker at dx=35 destroyed, marker at dx=38 intact
execute unless block 435 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 438 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
# tier 10 (r=128): marker at dx=20 destroyed, marker at dx=130 intact
execute unless block 1120 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
execute if block 1230 -60 0 minecraft:air run scoreboard players set chk_radius tnttest 0
say CHECK_RADIUS_DONE
