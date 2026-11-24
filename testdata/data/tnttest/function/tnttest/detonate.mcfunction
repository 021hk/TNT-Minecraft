say TNTTEST_DETONATE
# a redstone block next to every TNT primes it through the vanilla redstone path
setblock 1 -60 0 minecraft:redstone_block
setblock 201 -60 0 minecraft:redstone_block
setblock 401 -60 0 minecraft:redstone_block
setblock 601 -60 9 minecraft:redstone_block
setblock 651 -60 9 minecraft:redstone_block
setblock 681 -60 9 minecraft:redstone_block
setblock 1101 -60 0 minecraft:redstone_block
setblock 761 -60 9 minecraft:redstone_block
setblock 850 -60 0 minecraft:redstone_block
setblock 899 -60 0 minecraft:redstone_block
# drop test: "mining" the double compressed TNT must drop itself
kill @e[type=minecraft:item]
setblock 800 -60 0 minecraft:air destroy
say TNTTEST_DETONATE_DONE
