scoreboard players set chk_t10 tnttest 1
# tier 10 (r=80): marker at dx=79 destroyed, marker at dx=82 intact
execute unless block 1079 -60 0 minecraft:air run scoreboard players set chk_t10 tnttest 0
execute if block 1082 -60 0 minecraft:air run scoreboard players set chk_t10 tnttest 0
# stone cube inside the tier-10 blast must be fully vaporized
execute unless block 992 -58 0 minecraft:air run scoreboard players set chk_t10 tnttest 0
execute unless block 990 -56 -2 minecraft:air run scoreboard players set chk_t10 tnttest 0
execute unless block 994 -60 2 minecraft:air run scoreboard players set chk_t10 tnttest 0
