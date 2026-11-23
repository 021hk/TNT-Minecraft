scoreboard players set chk_drops tnttest 0
execute if entity @e[type=minecraft:item,nbt={Item:{id:"compressedtnt:double_compressed_tnt"}}] run scoreboard players set chk_drops tnttest 1
execute if entity @e[type=minecraft:item] run say DIAG_ITEM_PRESENT
