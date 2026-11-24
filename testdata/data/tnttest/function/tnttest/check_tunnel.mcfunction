scoreboard players set chk_tunnel_h tnttest 1
scoreboard players set chk_tunnel_v tnttest 1
# ---- horizontal tunnel: path 851..862 bored at y=-60 AND y=-59 ----
execute unless block 851 -60 0 minecraft:air run scoreboard players set chk_tunnel_h tnttest 0
execute unless block 856 -60 0 minecraft:air run scoreboard players set chk_tunnel_h tnttest 0
execute unless block 862 -60 0 minecraft:air run scoreboard players set chk_tunnel_h tnttest 0
execute unless block 856 -59 0 minecraft:air run scoreboard players set chk_tunnel_h tnttest 0
# beyond the end and beside the tunnel must survive
execute if block 863 -60 0 minecraft:air run scoreboard players set chk_tunnel_h tnttest 0
execute if block 856 -60 3 minecraft:air run scoreboard players set chk_tunnel_h tnttest 0
# ---- vertical tunnel: shaft 900 y=-60..-71 ----
execute unless block 900 -60 0 minecraft:air run scoreboard players set chk_tunnel_v tnttest 0
execute unless block 900 -66 0 minecraft:air run scoreboard players set chk_tunnel_v tnttest 0
execute unless block 900 -71 0 minecraft:air run scoreboard players set chk_tunnel_v tnttest 0
# below the shaft and beside it must survive
execute if block 900 -72 0 minecraft:air run scoreboard players set chk_tunnel_v tnttest 0
execute if block 901 -65 0 minecraft:air run scoreboard players set chk_tunnel_v tnttest 0
say CHECK_TUNNEL_DONE
