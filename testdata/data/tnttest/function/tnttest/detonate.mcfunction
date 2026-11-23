say TNTTEST_DETONATE
# redstone placed AFTER the tnt so the neighbor update primes it
setblock 1 -60 0 minecraft:redstone_block
setblock 201 -60 0 minecraft:redstone_block
setblock 401 -60 0 minecraft:redstone_block
setblock 601 -60 0 minecraft:redstone_block
setblock 1001 -60 0 minecraft:redstone_block
# armed timer: 3 seconds auto-explode
setblock 800 -60 0 compressedtnt:compressed_tnt[fuse=3]{IgniteRemaining:60}
# mining/drops test: blocks inside spawn chunks (always ticking)
setblock 30 -60 0 compressedtnt:double_compressed_tnt
setblock 40 -60 0 compressedtnt:double_compressed_tnt
