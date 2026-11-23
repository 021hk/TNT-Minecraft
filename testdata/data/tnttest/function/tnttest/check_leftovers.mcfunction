scoreboard players set chk_left tnttest 1
# probes BEFORE any fill changes the area
execute unless block 400 -61 0 minecraft:air run say DIAG_400_61_NOT_AIR
execute unless block 400 -63 0 minecraft:air run say DIAG_400_63_NOT_AIR
execute unless block 400 -59 0 minecraft:air run say DIAG_400_59_NOT_AIR
execute unless block 395 -61 0 minecraft:air run say DIAG_395_61_NOT_AIR
execute unless block 405 -61 0 minecraft:air run say DIAG_405_61_NOT_AIR
execute unless block 419 -63 15 minecraft:air run say DIAG_419_63_NOT_AIR
execute unless block 380 -45 0 minecraft:air run say DIAG_380_45_NOT_AIR
# every non-air block inside the 40x19x30 cube around the tier-3 crater (r=36)
# must be gone - the whole cube lies inside the blast sphere
execute store result score tnttest:air tnttest run fill 380 -63 -14 419 -45 15 minecraft:barrier replace minecraft:air
scoreboard players set tnttest:vol tnttest 22800
scoreboard players operation tnttest:rem tnttest = tnttest:vol tnttest
scoreboard players operation tnttest:rem tnttest -= tnttest:air tnttest
execute if score tnttest:rem tnttest matches 1 run say DIAG_LEFTOVERS_EXACTLY_1
execute if score tnttest:rem tnttest matches 2..10 run say DIAG_LEFTOVERS_2_10
execute if score tnttest:rem tnttest matches 11..100 run say DIAG_LEFTOVERS_11_100
execute if score tnttest:rem tnttest matches 101..1000 run say DIAG_LEFTOVERS_101_1000
execute if score tnttest:rem tnttest matches 1001.. run say DIAG_LEFTOVERS_1000_PLUS
execute if score tnttest:rem tnttest matches 1.. run scoreboard players set chk_left tnttest 0
fill 380 -63 -14 419 -45 15 minecraft:air replace minecraft:barrier
say CHECK_LEFTOVERS_DONE
