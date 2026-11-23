say FUNC_TEST_START
kill @e[type=minecraft:item]
setblock 860 -60 0 compressedtnt:double_compressed_tnt
setblock 860 -60 0 minecraft:air destroy
execute if entity @e[type=minecraft:item] run say FUNC_SAW_ITEMS
execute as @e[type=minecraft:item] run data get entity @s Item
say FUNC_TEST_END
