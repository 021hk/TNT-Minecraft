say TNTTEST_V4_START
scoreboard objectives add tnttest dummy
forceload add -32 -16 1250 16
kill @e[type=minecraft:item]

# ===== Area A: tier 1 (center 0, radius 8) =====
setblock 0 -60 0 compressedtnt:compressed_tnt
setblock 7 -60 0 minecraft:stone
setblock 9 -60 0 minecraft:stone

# ===== Area B: tier 2 (center 200, radius 17) =====
setblock 200 -60 0 compressedtnt:double_compressed_tnt
setblock 215 -60 0 minecraft:stone
setblock 218 -60 0 minecraft:stone

# ===== Area C: tier 3 (center 400, radius 36) =====
setblock 400 -60 0 compressedtnt:triple_compressed_tnt
setblock 435 -60 0 minecraft:stone
setblock 438 -60 0 minecraft:stone

# ===== Area D: tier 10 (center 1100, radius 128) =====
setblock 1100 -60 0 compressedtnt:decuple_compressed_tnt
setblock 1120 -60 0 minecraft:stone
setblock 1230 -60 0 minecraft:stone

# ===== Area E1: dirt TNT (center 600,9) =====
fill 595 -61 4 604 -61 13 minecraft:dirt
fill 597 -60 6 599 -60 8 minecraft:dirt
fill 599 -60 8 601 -60 10 minecraft:dirt
setblock 602 -61 6 minecraft:stone
summon minecraft:cow 596 -58 5 {NoAI:1b, PersistenceRequired:1b}
setblock 600 -60 9 compressedtnt:dirt_tnt

# ===== Area E2: stone TNT (center 650,9) =====
fill 645 -61 4 654 -61 13 minecraft:stone
fill 645 -60 4 654 -60 13 minecraft:stone
setblock 650 -60 9 compressedtnt:stone_tnt

# ===== Area E3: crop TNT (center 680,9) =====
fill 675 -60 4 684 -60 13 minecraft:farmland
fill 675 -59 4 684 -59 13 minecraft:wheat[age=7]
setblock 680 -60 9 compressedtnt:crop_tnt

# ===== Area F: fuse timer (summoned with custom fuse, 40 ticks = 2 s) =====
summon compressedtnt:primed_compressed_tnt 700 -60 0 {CompressedTier:1,CompressedFuse:40}
setblock 706 -60 0 minecraft:stone
setblock 709 -60 0 minecraft:stone
summon minecraft:tnt 730 -60 0 {Fuse:40}

# ===== Area G: block drop (mining equivalent) =====
setblock 800 -60 0 compressedtnt:double_compressed_tnt

# water source just outside the tier-2 sphere (x=218, dx=18 > 17):
# after the blast it must flow into the crater
setblock 218 -58 0 minecraft:water

scoreboard players set chk_radius tnttest 0
scoreboard players set chk_left tnttest 0
scoreboard players set chk_water tnttest 0
scoreboard players set chk_dirt tnttest 0
scoreboard players set chk_stone tnttest 0
scoreboard players set chk_crop tnttest 0
scoreboard players set chk_items tnttest 0
scoreboard players set chk_nodmg tnttest 0
scoreboard players set chk_fuse tnttest 0
scoreboard players set chk_vfuse tnttest 0
scoreboard players set chk_drops tnttest 0
scoreboard players set chk_ents tnttest 0
say TNTTEST_SETUP_DONE
