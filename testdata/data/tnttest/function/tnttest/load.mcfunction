say TEST_START
gamerule spawnChunkRadius 24
scoreboard objectives add tnttest dummy
forceload add -40 -90 210 110
# --- Area A: full-destroy test (decuple tier, fuse=2s) inside a 30x30x30 stone cube (fill limit = 32768) ---
fill 15 -63 15 44 -34 44 minecraft:stone
setblock 29 -60 29 minecraft:redstone_block
setblock 29 -59 29 compressedtnt:decuple_compressed_tnt[fuse=2]
# --- Area C: fuse wiring test (double tier, fuse=1s) ---
setblock 100 -60 -20 minecraft:redstone_block
setblock 100 -59 -20 compressedtnt:double_compressed_tnt[fuse=1]
setblock 130 -60 -20 minecraft:stone
# --- fuse property range test: 120s state must be accepted ---
setblock 100 -59 -80 compressedtnt:compressed_tnt[fuse=120]
execute if block 100 -59 -80 compressedtnt:compressed_tnt[fuse=120] run say CHECK_FUSE120_SET
setblock 100 -59 -80 minecraft:air
schedule function tnttest:tnttest/check 45s
schedule function tnttest:tnttest/check2 65s
