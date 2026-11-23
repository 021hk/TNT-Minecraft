say TNTTEST_REPORT
execute if score chk_drops tnttest matches 1 run say CHECK_DROPS PASS
execute if score chk_drops tnttest matches 0 run say CHECK_DROPS FAIL
execute if score chk_fuse tnttest matches 1 run say CHECK_FUSE_AUTO PASS
execute if score chk_fuse tnttest matches 0 run say CHECK_FUSE_AUTO FAIL
execute if score chk_control tnttest matches 1 run say CHECK_CONTROL_NORMAL PASS
execute if score chk_control tnttest matches 0 run say CHECK_CONTROL_NORMAL FAIL
execute if score chk_radius tnttest matches 1 run say CHECK_RADIUS_TIERS PASS
execute if score chk_radius tnttest matches 0 run say CHECK_RADIUS_TIERS FAIL
execute if score chk_left tnttest matches 1 run say CHECK_NO_LEFTOVERS PASS
execute if score chk_left tnttest matches 0 run say CHECK_NO_LEFTOVERS FAIL
execute if score chk_water tnttest matches 1 run say CHECK_WATER_FLOW PASS
execute if score chk_water tnttest matches 0 run say CHECK_WATER_FLOW FAIL
execute if score chk_t10 tnttest matches 1 run say CHECK_T10_RADIUS PASS
execute if score chk_t10 tnttest matches 0 run say CHECK_T10_RADIUS FAIL
execute if score chk_ents tnttest matches 1 run say CHECK_ENTITIES_CLEAN PASS
execute if score chk_ents tnttest matches 0 run say CHECK_ENTITIES_CLEAN FAIL
execute if score chk_drops tnttest matches 1 if score chk_fuse tnttest matches 1 if score chk_control tnttest matches 1 if score chk_radius tnttest matches 1 if score chk_left tnttest matches 1 if score chk_water tnttest matches 1 if score chk_t10 tnttest matches 1 if score chk_ents tnttest matches 1 run say TNTTEST_ALL_PASS
