scoreboard players set chk_fuse tnttest 1
scoreboard players set chk_control tnttest 1
# armed block (3s timer) must have self-ignited and exploded
execute if block 800 -60 0 compressedtnt:compressed_tnt run scoreboard players set chk_fuse tnttest 0
execute if block 807 -60 0 minecraft:stone run scoreboard players set chk_fuse tnttest 0
# control block (no timer set) must still be there = normal vanilla behaviour
execute unless block 830 -60 0 compressedtnt:compressed_tnt run scoreboard players set chk_control tnttest 0
