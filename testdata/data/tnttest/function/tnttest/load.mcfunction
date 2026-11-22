say TEST_START
forceload add -64 -64 96 96
setblock 20 -60 20 minecraft:redstone_block
setblock 20 -59 20 compressedtnt:compressed_tnt
schedule function tnttest:tnttest/check 60s
