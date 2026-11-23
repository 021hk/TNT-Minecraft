scoreboard players set chk_dirt tnttest 1
scoreboard players set chk_stone tnttest 1
scoreboard players set chk_crop tnttest 1
scoreboard players set chk_items tnttest 1
scoreboard players set chk_nodmg tnttest 1
# ---- dirt TNT: every dirt block of the 10x10x10 area gone ----
execute if block 595 -61 4 minecraft:dirt run scoreboard players set chk_dirt tnttest 0
execute if block 600 -61 9 minecraft:dirt run scoreboard players set chk_dirt tnttest 0
execute if block 598 -60 7 minecraft:dirt run scoreboard players set chk_dirt tnttest 0
# stone marker inside the dirt area survived, grass just outside survived
execute unless block 602 -61 6 minecraft:stone run scoreboard players set chk_dirt tnttest 0
execute unless block 593 -61 9 minecraft:grass_block run scoreboard players set chk_dirt tnttest 0
# ---- stone TNT: stone gone, cobblestone dropped, dirt below untouched ----
execute if block 645 -61 4 minecraft:stone run scoreboard players set chk_stone tnttest 0
execute if block 650 -61 9 minecraft:stone run scoreboard players set chk_stone tnttest 0
execute if block 650 -60 4 minecraft:stone run scoreboard players set chk_stone tnttest 0
execute unless block 650 -62 9 minecraft:dirt run scoreboard players set chk_stone tnttest 0
# ---- crop TNT: wheat harvested, farmland intact ----
execute if block 675 -59 4 minecraft:wheat run scoreboard players set chk_crop tnttest 0
execute if block 680 -59 9 minecraft:wheat run scoreboard players set chk_crop tnttest 0
execute unless block 675 -60 4 minecraft:farmland run scoreboard players set chk_crop tnttest 0
# ---- item drops on the ground ----
execute unless entity @e[type=minecraft:item,nbt={Item:{id:"minecraft:dirt"}}] run scoreboard players set chk_items tnttest 0
execute unless entity @e[type=minecraft:item,nbt={Item:{id:"minecraft:cobblestone"}}] run scoreboard players set chk_items tnttest 0
execute unless entity @e[type=minecraft:item,nbt={Item:{id:"minecraft:wheat"}}] run scoreboard players set chk_items tnttest 0
# ---- the cow inside the dirt area must be completely unharmed ----
execute unless entity @e[type=minecraft:cow] run say DIAG_COW_DEAD
execute unless entity @e[type=minecraft:cow] run scoreboard players set chk_nodmg tnttest 0
# diagnostics
execute if block 595 -61 4 minecraft:dirt run say DIAG_DIRT_STILL_THERE
execute unless block 602 -61 6 minecraft:stone run say DIAG_602_STONE_GONE
execute unless block 593 -61 9 minecraft:grass_block run say DIAG_593_GRASS_GONE
execute if block 650 -60 4 minecraft:stone run say DIAG_650STONE_STILL_THERE
execute unless block 650 -62 9 minecraft:dirt run say DIAG_650DIRT_GONE
execute unless block 675 -60 4 minecraft:farmland run say DIAG_FARMLAND_GONE
execute if block 675 -59 4 minecraft:wheat run say DIAG_WHEAT_STILL_THERE
say CHECK_MATERIAL_DONE
