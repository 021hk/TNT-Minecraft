# count leftover non-air blocks inside the 30x30x30 test cube (volume = 27000)
execute store result score tnttest:air tnttest run fill 15 -63 15 44 -34 44 minecraft:barrier replace minecraft:air
scoreboard players set tnttest:vol tnttest 27000
scoreboard players operation tnttest:rem tnttest = tnttest:vol tnttest
scoreboard players operation tnttest:rem tnttest -= tnttest:air tnttest
execute if score tnttest:rem tnttest matches 0 run say CHECK_NO_LEFTOVERS
execute if score tnttest:rem tnttest matches 1.. run say CHECK_LEFTOVERS_FOUND
tellraw @a {"score":{"name":"tnttest:rem","objective":"tnttest"},"extra":[{"text":" blocks left inside the cube"}]}
fill 15 -63 15 44 -34 44 minecraft:air replace minecraft:barrier
# drop water next to the crater rim: it must flow back into the hole (fluid physics fix)
setblock 95 -59 29 minecraft:water
# area C marker (13 blocks from a double tier with fuse=1s) must be vaporized
execute if block 130 -60 -20 minecraft:air run say CHECK_FUSE1_DETONATED
say CHECK1_DONE
