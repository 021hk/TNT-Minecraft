say TNTTEST_V3_START
scoreboard objectives add tnttest dummy
forceload add -32 -16 1120 16
scoreboard players set chk_radius tnttest 0
scoreboard players set chk_left tnttest 0
scoreboard players set chk_water tnttest 0
scoreboard players set chk_fuse tnttest 0
scoreboard players set chk_control tnttest 0
scoreboard players set chk_drops tnttest 0
scoreboard players set chk_t10 tnttest 0
scoreboard players set chk_ents tnttest 0

# ===== Area A: tier 1 (center 0, r=8) =====
setblock 0 -60 0 compressedtnt:compressed_tnt
setblock 7 -60 0 minecraft:stone
setblock 9 -60 0 minecraft:stone

# ===== Area B: tier 2 (center 200, r=16) =====
setblock 200 -60 0 compressedtnt:double_compressed_tnt
setblock 215 -60 0 minecraft:stone
setblock 218 -60 0 minecraft:stone

# ===== Area C: tier 3 (center 400, r=24) + leftovers cube =====
setblock 400 -60 0 compressedtnt:triple_compressed_tnt
setblock 423 -60 0 minecraft:stone
setblock 426 -60 0 minecraft:stone
fill 390 -60 -2 394 -56 2 minecraft:stone

# ===== Area W: water flow-back (tier 2, center 600, r=16) =====
setblock 600 -60 0 compressedtnt:double_compressed_tnt
setblock 617 -60 0 minecraft:water
setblock 617 -59 0 minecraft:water
setblock 617 -58 0 minecraft:water

# ===== Area F: fuse timer (center 800) + control (830) =====
setblock 830 -60 0 compressedtnt:compressed_tnt
setblock 807 -60 0 minecraft:stone

# ===== Area N: tier 10 (center 1000, r=80) + leftovers cube =====
setblock 1000 -60 0 compressedtnt:decuple_compressed_tnt
setblock 1079 -60 0 minecraft:stone
setblock 1082 -60 0 minecraft:stone
fill 990 -60 -2 994 -56 2 minecraft:stone

schedule function tnttest:tnttest/detonate 1s
schedule function tnttest:tnttest/drops_destroy 40t
schedule function tnttest:tnttest/check_drops 6s
schedule function tnttest:tnttest/check_fuse 6s
schedule function tnttest:tnttest/check_radius 10s
schedule function tnttest:tnttest/check_radius2 11s
schedule function tnttest:tnttest/check_t10 14s
schedule function tnttest:tnttest/check_entities 100s
schedule function tnttest:tnttest/report 101s
