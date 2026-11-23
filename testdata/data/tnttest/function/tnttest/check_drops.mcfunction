scoreboard players set chk_drops tnttest 1
# the destroyed double compressed TNT must have dropped itself as an item
execute unless entity @e[type=minecraft:item,nbt={Item:{id:"compressedtnt:double_compressed_tnt"}}] run scoreboard players set chk_drops tnttest 0
say CHECK_DROPS_DONE
