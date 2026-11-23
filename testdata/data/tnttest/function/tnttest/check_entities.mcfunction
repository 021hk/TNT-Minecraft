scoreboard players set chk_ents tnttest 1
# every primed entity must be discarded once its wave/sweep is done
execute if entity @e[type=compressedtnt:primed_compressed_tnt] run scoreboard players set chk_ents tnttest 0
execute if entity @e[type=compressedtnt:material_tnt] run scoreboard players set chk_ents tnttest 0
say CHECK_ENTITIES_DONE
