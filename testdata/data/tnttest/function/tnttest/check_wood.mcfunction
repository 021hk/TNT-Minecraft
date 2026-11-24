scoreboard players set chk_wood tnttest 1
# ---- wood TNT: every log and plank of the 10x10x10 area gone ----
execute if block 755 -61 4 minecraft:oak_log run scoreboard players set chk_wood tnttest 0
execute if block 764 -61 13 minecraft:oak_log run scoreboard players set chk_wood tnttest 0
execute if block 757 -61 6 minecraft:oak_log run scoreboard players set chk_wood tnttest 0
execute if block 758 -60 7 minecraft:oak_planks run scoreboard players set chk_wood tnttest 0
# stone marker inside the area and grass just outside survived
execute unless block 762 -61 6 minecraft:stone run scoreboard players set chk_wood tnttest 0
execute unless block 754 -61 9 minecraft:grass_block run scoreboard players set chk_wood tnttest 0
# item drops on the ground
execute unless entity @e[type=minecraft:item,nbt={Item:{id:"minecraft:oak_log"}}] run scoreboard players set chk_wood tnttest 0
execute unless entity @e[type=minecraft:item,nbt={Item:{id:"minecraft:oak_planks"}}] run scoreboard players set chk_wood tnttest 0
say CHECK_WOOD_DONE
