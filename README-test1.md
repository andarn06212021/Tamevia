# Tamevia test1

Included:
- Rolling server logs via log4j2.xml (daily + size rotation)

Part 2 (next) will add:
- Client crash guards (render/camera bounds)
- Safer PacketHandler logging and checks
- Server tick isolation (fixed-rate + try/catch)
- Plugin per-invocation try/catch and slow warnings

Run:
- Start-Windows.cmd -> press 1
If client won’t connect:
- Client_Base\Cache\host.txt = 127.0.0.1
- Client_Base\Cache\port.txt = 43595
