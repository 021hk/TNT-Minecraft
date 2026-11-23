say TNTTEST_REPORT
execute if score chk_drops tnttest matches 1 run say CHECK_DROPS PASS
execute if score chk_drops tnttest matches 0 run say CHECK_DROPS FAIL
execute if score chk_fuse tnttest matches 1 run say CHECK_FUSE_CUSTOM PASS
execute if score chk_fuse tnttest matches 0 run say CHECK_FUSE_CUSTOM FAIL
execute if score chk_vfuse tnttest matches 1 run say CHECK_VANILLA_FUSE PASS
execute if score chk_vfuse tnttest matches 0 run say CHECK_VANILLA_FUSE FAIL
execute if score chk_radius tnttest matches 1 run say CHECK_RADIUS_TIERS PASS
execute if score chk_radius tnttest matches 0 run say CHECK_RADIUS_TIERS FAIL
execute if score chk_left tnttest matches 1 run say CHECK_NO_LEFTOVERS PASS
execute if score chk_left tnttest matches 0 run say CHECK_NO_LEFTOVERS FAIL
execute if score chk_water tnttest matches 1 run say CHECK_WATER_FLOW PASS
execute if score chk_water tnttest matches 0 run say CHECK_WATER_FLOW FAIL
execute if score chk_dirt tnttest matches 1 run say CHECK_DIRT_TNT PASS
execute if score chk_dirt tnttest matches 0 run say CHECK_DIRT_TNT FAIL
execute if score chk_stone tnttest matches 1 run say CHECK_STONE_TNT PASS
execute if score chk_stone tnttest matches 0 run say CHECK_STONE_TNT FAIL
execute if score chk_crop tnttest matches 1 run say CHECK_CROP_TNT PASS
execute if score chk_crop tnttest matches 0 run say CHECK_CROP_TNT FAIL
execute if score chk_items tnttest matches 1 run say CHECK_MATERIAL_ITEMS PASS
execute if score chk_items tnttest matches 0 run say CHECK_MATERIAL_ITEMS FAIL
execute if score chk_nodmg tnttest matches 1 run say CHECK_NO_DAMAGE PASS
execute if score chk_nodmg tnttest matches 0 run say CHECK_NO_DAMAGE FAIL
execute if score chk_ents tnttest matches 1 run say CHECK_ENTITIES_CLEAN PASS
execute if score chk_ents tnttest matches 0 run say CHECK_ENTITIES_CLEAN FAIL
execute if score chk_drops tnttest matches 1 if score chk_fuse tnttest matches 1 if score chk_vfuse tnttest matches 1 if score chk_radius tnttest matches 1 if score chk_left tnttest matches 1 if score chk_water tnttest matches 1 if score chk_dirt tnttest matches 1 if score chk_stone tnttest matches 1 if score chk_crop tnttest matches 1 if score chk_items tnttest matches 1 if score chk_nodmg tnttest matches 1 if score chk_ents tnttest matches 1 run say TNTTEST_ALL_PASS
