package com.runelive.world.clip.region;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

import com.runelive.model.GameObject;
import com.runelive.model.Locations.Location;
import com.runelive.model.Position;
import com.runelive.model.definitions.GameObjectDefinition;
import com.runelive.world.entity.impl.Character;

/**
 * A highly modified version of the released clipping.
 *
 * @author Relex lawl and Palidino: Gave me (Gabbe) the base.
 * @editor Gabbe: Rewrote the system, now loads regions when they're actually needed etc.
 */
public final class RegionClipping {

    private static final Map<Integer, RegionClipping> regions = new ConcurrentHashMap<>();

    private int id;

    private int[][][] clips = new int[4][][];
    public GameObject[][][] gameObjects = new GameObject[4][][];

    public RegionClipping(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void removeClip(int x, int y, int height, int shift) {
        int regionAbsX = (id >> 8) * 64;
        int regionAbsY = (id & 0xff) * 64;
        if (height < 0 || height >= 4)
            height = 0;
        if (clips[height] == null) {
            clips[height] = new int[64][64];
        }
        clips[height][x - regionAbsX][y - regionAbsY] = /* 16777215 - shift */0;
    }

    public void addClip(int x, int y, int height, int shift) {
        int regionAbsX = (id >> 8) * 64;
        int regionAbsY = (id & 0xff) * 64;
        if (height < 0 || height >= 4)
            height = 0;
        if (clips[height] == null) {
            clips[height] = new int[64][64];
        }
        clips[height][x - regionAbsX][y - regionAbsY] |= shift;
    }

    // TODO: Move to a utility class
    static byte[] degzip(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        if (bytes.length == 0) {
            return bytes;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            byte[] buffer = new byte[Byte.BYTES * 1024];

            while (true) {
                int read = gzip.read(buffer);
                if (read == -1) {
                    break;
                }

                os.write(buffer, 0, read);
            }
        }
        return os.toByteArray();
    }

    /**
     * Reads a 'smart' (either a {@code byte} or {@code short} depending on the value) from the
     * specified buffer.
     *
     * @param buffer The buffer.
     * @return The 'smart'.
     */
    // TODO: Move to a utility class
    public static int readSmart(ByteBuffer buffer) {
        int peek = buffer.get(buffer.position()) & 0xFF;
        if (peek < 128) {
            return buffer.get() & 0xFF;
        }
        return (buffer.getShort() & 0xFFFF) - 32768;
    }

    public static void init() throws IOException {

        class MapDefinition {
            private final int id;
            private final int objects;
            private final int terrain;

            public MapDefinition(int id, int objects, int terrain) {
                this.id = id;
                this.objects = objects;
                this.terrain = terrain;
            }

        }

        GameObjectDefinition.init();

        Path path = Paths.get("data", "clipping");

        // Decodes map definitions
        ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(path.resolve("map_index")));
        Map<Integer, MapDefinition> definitions = new HashMap<>();
        int size = buffer.getShort() & 0xFFFF;
        for (int i = 0; i < size; i++) {
            int id = buffer.getShort() & 0xFFFF;
            int terrain = buffer.getShort() & 0xFFFF;
            int objects = buffer.getShort() & 0xFFFF;
            definitions.put(id, new MapDefinition(id, objects, terrain));
            regions.computeIfAbsent(id, RegionClipping::new);
        }

        // Decodes terrain and objects
        for (MapDefinition definition : definitions.values()) {
            int id = definition.id;
            int x = (id >> 8) * 64;
            int y = (id & 0xFF) * 64;

            byte[] objects = degzip(path.resolve("maps").resolve(definition.objects + ".gz"));
            byte[] terrain = degzip(path.resolve("maps").resolve(definition.terrain + ".gz"));

            if (objects.length == 0) {
                //System.out.println("Objects for region: [x, y, id, file] - [" + x + ", " + y + ", " + id + ", " + definition.objects + "] do not exist.");
                continue;
            }

            if (terrain.length == 0) {
                //System.out.println("Terrain for region: [x, y, id, file] - [" + x + ", " + y + ", " + id + ", " + definition.terrain + "] does not exist.");
                continue;
            }

            loadMaps(x, y, ByteBuffer.wrap(objects), ByteBuffer.wrap(terrain));
        }

        System.out.println("Loaded " + definitions.size() + " map definitions.");
    }

    private static void loadMaps(int absX, int absY, ByteBuffer objectStream,
                                 ByteBuffer groundStream) {
        byte[][][] heightMap = new byte[4][64][64];
        try {
            for (int z = 0; z < 4; z++) {
                for (int tileX = 0; tileX < 64; tileX++) {
                    for (int tileY = 0; tileY < 64; tileY++) {
                        while (true) {
                            int tileType = groundStream.get() & 0xFF;
                            if (tileType == 0) {
                                break;
                            } else if (tileType == 1) {
                                groundStream.get();
                                break;
                            } else if (tileType <= 49) {
                                groundStream.get();
                            } else if (tileType <= 81) {
                                heightMap[z][tileX][tileY] = (byte) (tileType - 49);
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < 4; i++) {
                for (int i2 = 0; i2 < 64; i2++) {
                    for (int i3 = 0; i3 < 64; i3++) {
                        if ((heightMap[i][i2][i3] & 1) == 1) {
                            int height = i;
                            if ((heightMap[1][i2][i3] & 2) == 2) {
                                height--;
                            }
                            if (height >= 0 && height <= 3) {
                                addClipping(absX + i2, absY + i3, height, 0x200000);
                            }
                        }
                    }
                }
            }
            int objectId = -1;
            int incr;
            while ((incr = readSmart(objectStream)) != 0) {
                objectId += incr;
                int location = 0;
                int incr2;
                while ((incr2 = readSmart(objectStream)) != 0) {
                    location += incr2 - 1;
                    int localX = location >> 6 & 0x3f;
                    int localY = location & 0x3f;
                    int height = location >> 12;
                    int objectData = objectStream.get() & 0xFF;
                    int type = objectData >> 2;
                    int direction = objectData & 0x3;
                    if (localX < 0 || localX >= 64 || localY < 0 || localY >= 64) {
                        continue;
                    }
                    if ((heightMap[1][localX][localY] & 2) == 2) {
                        height--;
                    }
                    if (height >= 0 && height <= 3)
                        addObject(objectId, absX + localX, absY + localY, height, type, direction); // Add
                    // object
                    // to
                    // clipping
                }
            }
        } catch (Exception cause) {
            System.out.println("Unable to load maps in region: "
                    + ((((absX >> 3) / 8) << 8) + ((absY >> 3) / 8)) + " pos: " + absX + ", " + absY);
        }
    }

    public static void addClipping(int x, int y, int height, int shift) {
        // System.out.println("Added clip at "+x+" and "+y+"");
        int regionX = x >> 3;
        int regionY = y >> 3;
        int regionId = (regionX / 8 << 8) + regionY / 8;
        RegionClipping r = regions.get(regionId);
        if (r != null) {
            r.addClip(x, y, height, shift);
        }
    }

    public static void removeClipping(int x, int y, int height, int shift) {
        int regionX = x >> 3;
        int regionY = y >> 3;
        int regionId = (regionX / 8 << 8) + regionY / 8;

        RegionClipping r = regions.get(regionId);
        if (r != null) {
            r.removeClip(x, y, height, shift);
        }
    }

    public static RegionClipping forPosition(Position position) {
        int regionX = position.getX() >> 3;
        int regionY = position.getY() >> 3;
        int regionId = ((regionX / 8) << 8) + (regionY / 8);
        return regions.get(regionId);
    }

    public static int[] getObjectInformation(Position position) {
        final RegionClipping clipping = forPosition(position);
        if (clipping != null) {
            int x = position.getX();
            int y = position.getY();
            int height = position.getZ();
            int regionAbsX = (clipping.id >> 8) * 64;
            int regionAbsY = (clipping.id & 0xff) * 64;
            if (height < 0 || height >= 4)
                height = 0;
            if (clipping.gameObjects == null || clipping.gameObjects[height] == null
                    || clipping.gameObjects[height][x - regionAbsX] == null
                    || clipping.gameObjects[height][x - regionAbsX][y - regionAbsY] == null) {
                return null;
            }
            return new int[]{clipping.gameObjects[height][x - regionAbsX][y - regionAbsY].getFace(),
                    clipping.gameObjects[height][x - regionAbsX][y - regionAbsY].getType(),
                    clipping.gameObjects[height][x - regionAbsX][y - regionAbsY].getId(),};
        } else {
            return null;
        }
    }

    public static boolean objectExists(GameObject object) {
        Location loc = Location.getLocation(object);
        Position pos = object.getPosition();
        int id = object.getId();
        boolean barrows = pos.getZ() == -1 && object.getDefinition() != null
                && (object.getDefinition().getName().toLowerCase().contains("sarcophagus")
                || object.getDefinition().getName().toLowerCase().contains("staircase"))
                || loc != null && loc == Location.BARROWS;
        boolean catherbyAquariums = id == 10091 && pos.getX() >= 2829 && pos.getX() <= 2832
                && pos.getY() >= 3441 && pos.getY() <= 3447;
        boolean freeForAllPortal = id == 38700 && pos.getX() == 2814 && pos.getY() == 5509;
        boolean warriorsGuild =
                id == 15653 && pos.getX() == 2877 && pos.getY() == 3546 || loc == Location.WARRIORS_GUILD;
        boolean fightPit = id == 9369 && pos.getX() == 2399 && pos.getY() == 5176
                || id == 9368 && pos.getX() == 2399 && pos.getY() == 5168;
        boolean barbCourseRopeswing = id == 2282 && pos.getX() == 2551 && pos.getY() == 3550;
        boolean lumbridgeCastle = id == 12348 && pos.getX() == 3207 && pos.getY() == 3217
                || id == 1738 && pos.getX() == 3204 && pos.getY() == 3207
                || id == 1739 && pos.getX() == 3204 && pos.getY() == 3207 && pos.getZ() == 1
                || id == 1739 && pos.getX() == 3204 && pos.getY() == 3229 && pos.getZ() == 1;
        boolean rfd = id == 12356 && (pos.getX() == 1900 && pos.getY() == 5345
                || pos.getX() == 1899 && pos.getY() == 5366 || pos.getX() == 1910 && pos.getY() == 5356
                || pos.getX() == 1889 && pos.getY() == 5355);
        boolean lunar = id == 29944 && pos.getX() == 2111 && pos.getY() == 3917;
        boolean chaosTunnels = id == 28779; // It checks player coords anyway
        boolean trees = id == 1306 && pos.getX() == 2696 && pos.getY() == 3423 || id == 1307
                && (pos.getX() == 2727 && pos.getY() == 3501 || pos.getX() == 2729 && pos.getY() == 3481);
        boolean godwars = pos.getZ() == 2;
        boolean lawAltar = id == 2485 && pos.getX() == 2463 && pos.getY() == 4831;
        boolean mageBankLever = id == 5959 && pos.getX() == 3090 && pos.getY() == 3956;
        boolean well = id == 884 && pos.getX() == 2811 && pos.getY() == 3347;
        boolean waterRcAltar = id == 2480 && pos.getX() == 3483 && pos.getY() == 4835;
        boolean crystalChest = id == 172 && pos.getX() == 3077 && pos.getY() == 3497;
        boolean wellOfGoodwill = id == 884 && pos.getX() == 3084 && pos.getY() == 3502;
        if (well || mageBankLever || lawAltar || trees || wellOfGoodwill || chaosTunnels || lunar
                || barrows || rfd || lumbridgeCastle || barbCourseRopeswing || catherbyAquariums
                || freeForAllPortal || warriorsGuild || fightPit || godwars || barrows || waterRcAltar
                || crystalChest)
            return true;
        int[] info = getObjectInformation(object.getPosition());
        if (info != null) {
            if (info[2] == object.getId()) {
                return true;
            }
        }
        return false;
    }

    public static GameObject getGameObject(Position position) {
        final RegionClipping clipping = forPosition(position);
        if (clipping != null) {
            int x = position.getX();
            int y = position.getY();
            int height = position.getZ();
            int regionAbsX = (clipping.id >> 8) * 64;
            int regionAbsY = (clipping.id & 0xff) * 64;
            if (height < 0 || height >= 4)
                height = 0;
            if (clipping.gameObjects[height] == null) {
                return null;
            }
            return clipping.gameObjects[height][x - regionAbsX][y - regionAbsY];
        } else {
            return null;
        }
    }

    private static void addClippingForVariableObject(int x, int y, int height, int type,
                                                     int direction, boolean flag) {
        if (type == 0) {
            if (direction == 0) {
                addClipping(x, y, height, 128);
                addClipping(x - 1, y, height, 8);
            } else if (direction == 1) {
                addClipping(x, y, height, 2);
                addClipping(x, y + 1, height, 32);
            } else if (direction == 2) {
                addClipping(x, y, height, 8);
                addClipping(x + 1, y, height, 128);
            } else if (direction == 3) {
                addClipping(x, y, height, 32);
                addClipping(x, y - 1, height, 2);
            }
        } else if (type == 1 || type == 3) {
            if (direction == 0) {
                addClipping(x, y, height, 1);
                addClipping(x - 1, y, height, 16);
            } else if (direction == 1) {
                addClipping(x, y, height, 4);
                addClipping(x + 1, y + 1, height, 64);
            } else if (direction == 2) {
                addClipping(x, y, height, 16);
                addClipping(x + 1, y - 1, height, 1);
            } else if (direction == 3) {
                addClipping(x, y, height, 64);
                addClipping(x - 1, y - 1, height, 4);
            }
        } else if (type == 2) {
            if (direction == 0) {
                addClipping(x, y, height, 130);
                addClipping(x - 1, y, height, 8);
                addClipping(x, y + 1, height, 32);
            } else if (direction == 1) {
                addClipping(x, y, height, 10);
                addClipping(x, y + 1, height, 32);
                addClipping(x + 1, y, height, 128);
            } else if (direction == 2) {
                addClipping(x, y, height, 40);
                addClipping(x + 1, y, height, 128);
                addClipping(x, y - 1, height, 2);
            } else if (direction == 3) {
                addClipping(x, y, height, 160);
                addClipping(x, y - 1, height, 2);
                addClipping(x - 1, y, height, 8);
            }
        }
        if (flag) {
            if (type == 0) {
                if (direction == 0) {
                    addClipping(x, y, height, 65536);
                    addClipping(x - 1, y, height, 4096);
                } else if (direction == 1) {
                    addClipping(x, y, height, 1024);
                    addClipping(x, y + 1, height, 16384);
                } else if (direction == 2) {
                    addClipping(x, y, height, 4096);
                    addClipping(x + 1, y, height, 65536);
                } else if (direction == 3) {
                    addClipping(x, y, height, 16384);
                    addClipping(x, y - 1, height, 1024);
                }
            }
            if (type == 1 || type == 3) {
                if (direction == 0) {
                    addClipping(x, y, height, 512);
                    addClipping(x - 1, y + 1, height, 8192);
                } else if (direction == 1) {
                    addClipping(x, y, height, 2048);
                    addClipping(x + 1, y + 1, height, 32768);
                } else if (direction == 2) {
                    addClipping(x, y, height, 8192);
                    addClipping(x + 1, y + 1, height, 512);
                } else if (direction == 3) {
                    addClipping(x, y, height, 32768);
                    addClipping(x - 1, y - 1, height, 2048);
                }
            } else if (type == 2) {
                if (direction == 0) {
                    addClipping(x, y, height, 66560);
                    addClipping(x - 1, y, height, 4096);
                    addClipping(x, y + 1, height, 16384);
                } else if (direction == 1) {
                    addClipping(x, y, height, 5120);
                    addClipping(x, y + 1, height, 16384);
                    addClipping(x + 1, y, height, 65536);
                } else if (direction == 2) {
                    addClipping(x, y, height, 20480);
                    addClipping(x + 1, y, height, 65536);
                    addClipping(x, y - 1, height, 1024);
                } else if (direction == 3) {
                    addClipping(x, y, height, 81920);
                    addClipping(x, y - 1, height, 1024);
                    addClipping(x - 1, y, height, 4096);
                }
            }
        }
    }

    private static void addClippingForSolidObject(int x, int y, int height, int xLength, int yLength,
                                                  boolean flag) {
        int clipping = 256;
        if (flag) {
            clipping += 0x20000;
        }
        for (int i = x; i < x + xLength; i++) {
            for (int i2 = y; i2 < y + yLength; i2++) {
                addClipping(i, i2, height, clipping);
            }
        }
    }

    public static int[] getLocalPosition(Position position) {
        final RegionClipping clipping = RegionClipping.forPosition(position);
        int absX = position.getX();
        int absY = position.getY();
        int regionAbsX = (clipping.getId() >> 8) * 64;
        int regionAbsY = (clipping.getId() & 0xff) * 64;
        int localX = absX - regionAbsX;
        int localY = absY - regionAbsY;
        return new int[]{localX, localY};
    }

    public static void addObject(int objectId, int x, int y, int height, int type, int direction) {
        if (GameObjectDefinition.removedObject(objectId))
            return;
        GameObjectDefinition def = GameObjectDefinition.forId(objectId);
        if (def == null) {
            return;
        }
        switch (objectId) {
            case 14233: // pest control gates
            case 14235: // pest control gates
                return;
        }
        final Position position = new Position(x, y, height);
        final RegionClipping clipping = forPosition(position);
        if (clipping != null) {
            if (clipping.gameObjects[height % 4] == null) {
                clipping.gameObjects[height % 4] = new GameObject[64][64];
            }
            int[] local = getLocalPosition(position);
            clipping.gameObjects[height % 4][local[0]][local[1]] =
                    new GameObject(objectId, new Position(x, y, height), type, direction);
        }
        if (objectId == -1) {
            removeClipping(x, y, height, 0x000000);
            return;
        }
        int xLength;
        int yLength;
        if (direction != 1 && direction != 3) {
            xLength = def.xLength();
            yLength = def.yLength();
        } else {
            yLength = def.xLength();
            xLength = def.yLength();
        }
        if (type == 22) {
            if (def.hasActions() && def.unwalkable) {
                addClipping(x, y, height, 0x200000);
            }
        } else if (type >= 9) {
            if (def.unwalkable) {
                addClippingForSolidObject(x, y, height, xLength, yLength, def.aBoolean779);
            }
        } else if (type >= 0 && type <= 3) {
            if (def.unwalkable) {
                addClippingForVariableObject(x, y, height, type, direction, def.aBoolean779);
            }
        }
    }

    public static void addObject(GameObject gameObject) {
        if (gameObject.getId() != 65535)
            addObject(gameObject.getId(), gameObject.getPosition().getX(),
                    gameObject.getPosition().getY(), gameObject.getPosition().getZ(), gameObject.getType(),
                    gameObject.getFace());
    }

    public static void removeObject(GameObject gameObject) {
        addObject(-1, gameObject.getPosition().getX(), gameObject.getPosition().getY(),
                gameObject.getPosition().getZ(), gameObject.getType(), gameObject.getFace());
    }

    public static int getClipping(int x, int y, int height) {
        int regionX = x >> 3;
        int regionY = y >> 3;
        int regionId = ((regionX / 8) << 8) + (regionY / 8);
        if (height >= 4)
            height = 0;
        else if (height == -1 || Location.inLocation(x, y, Location.PURO_PURO))
            return 0;
        RegionClipping r = regions.get(regionId);
        if (r != null) {
            return r.getClip(x, y, height);
        }
        return 0;
    }

    private int getClip(int x, int y, int height) {
        // height %= 4;
        int regionAbsX = (id >> 8) * 64;
        int regionAbsY = (id & 0xff) * 64;
        if (clips[height] == null) {
            clips[height] = new int[64][64];
        }
        return clips[height][x - regionAbsX][y - regionAbsY];
    }

    public static boolean canMove(int startX, int startY, int endX, int endY, int height, int xLength,
                                  int yLength) {
        int diffX = endX - startX;
        int diffY = endY - startY;
        int max = Math.max(Math.abs(diffX), Math.abs(diffY));
        for (int ii = 0; ii < max; ii++) {
            int currentX = endX - diffX;
            int currentY = endY - diffY;
            for (int i = 0; i < xLength; i++) {
                for (int i2 = 0; i2 < yLength; i2++)
                    if (diffX < 0 && diffY < 0) {
                        if ((getClipping((currentX + i) - 1, (currentY + i2) - 1, height) & 0x128010e) != 0
                                || (getClipping((currentX + i) - 1, currentY + i2, height) & 0x1280108) != 0
                                || (getClipping(currentX + i, (currentY + i2) - 1, height) & 0x1280102) != 0)
                            return false;
                    } else if (diffX > 0 && diffY > 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2 + 1, height) & 0x12801e0) != 0
                                || (getClipping(currentX + i + 1, currentY + i2, height) & 0x1280180) != 0
                                || (getClipping(currentX + i, currentY + i2 + 1, height) & 0x1280120) != 0)
                            return false;
                    } else if (diffX < 0 && diffY > 0) {
                        if ((getClipping((currentX + i) - 1, currentY + i2 + 1, height) & 0x1280138) != 0
                                || (getClipping((currentX + i) - 1, currentY + i2, height) & 0x1280108) != 0
                                || (getClipping(currentX + i, currentY + i2 + 1, height) & 0x1280120) != 0)
                            return false;
                    } else if (diffX > 0 && diffY < 0) {
                        if ((getClipping(currentX + i + 1, (currentY + i2) - 1, height) & 0x1280183) != 0
                                || (getClipping(currentX + i + 1, currentY + i2, height) & 0x1280180) != 0
                                || (getClipping(currentX + i, (currentY + i2) - 1, height) & 0x1280102) != 0)
                            return false;
                    } else if (diffX > 0 && diffY == 0) {
                        if ((getClipping(currentX + i + 1, currentY + i2, height) & 0x1280180) != 0)
                            return false;
                    } else if (diffX < 0 && diffY == 0) {
                        if ((getClipping((currentX + i) - 1, currentY + i2, height) & 0x1280108) != 0)
                            return false;
                    } else if (diffX == 0 && diffY > 0) {
                        if ((getClipping(currentX + i, currentY + i2 + 1, height) & 0x1280120) != 0)
                            return false;
                    } else if (diffX == 0 && diffY < 0
                            && (getClipping(currentX + i, (currentY + i2) - 1, height) & 0x1280102) != 0)
                        return false;

            }

            if (diffX < 0)
                diffX++;
            else if (diffX > 0)
                diffX--;
            if (diffY < 0)
                diffY++;
            else if (diffY > 0)
                diffY--;
        }

        return true;
    }

    public static boolean canMove(Position start, Position end, int xLength, int yLength) {
        return canMove(start.getX(), start.getY(), end.getX(), end.getY(), start.getZ(), xLength,
                yLength);
    }

    public static boolean blockedProjectile(Position position) {
        return (getClipping(position.getX(), position.getY(), position.getZ()) & 0x20000) == 0;
    }

    public static boolean blocked(Position pos) {
        return (getClipping(pos.getX(), pos.getY(), pos.getZ()) & 0x1280120) != 0;
    }

    public static boolean blockedNorth(Position pos) {
        return (getClipping(pos.getX(), pos.getY() + 1, pos.getZ()) & 0x1280120) != 0;
    }

    public static boolean blockedEast(Position pos) {
        return (getClipping(pos.getX() + 1, pos.getY(), pos.getZ()) & 0x1280180) != 0;
    }

    public static boolean blockedSouth(Position pos) {
        return (getClipping(pos.getX(), pos.getY() - 1, pos.getZ()) & 0x1280102) != 0;
    }

    public static boolean blockedWest(Position pos) {
        return (getClipping(pos.getX() - 1, pos.getY(), pos.getZ()) & 0x1280108) != 0;
    }

    public static boolean blockedNorthEast(Position pos) {
        return (getClipping(pos.getX() + 1, pos.getY() + 1, pos.getZ()) & 0x12801e0) != 0;
    }

    public static boolean blockedNorthWest(Position pos) {
        return (getClipping(pos.getX() - 1, pos.getY() + 1, pos.getZ()) & 0x1280138) != 0;
    }

    public static boolean blockedSouthEast(Position pos) {
        return (getClipping(pos.getX() + 1, pos.getY() - 1, pos.getZ()) & 0x1280183) != 0;
    }

    public static boolean blockedSouthWest(Position pos) {
        return (getClipping(pos.getX() - 1, pos.getY() - 1, pos.getZ()) & 0x128010e) != 0;
    }

    public static boolean canProjectileAttack(Character a, Character b) {
        if (!a.isPlayer()) {
            if (b.isPlayer()) {
                return canProjectileMove(b.getPosition().getX(), b.getPosition().getY(),
                        a.getPosition().getX(), a.getPosition().getY(), a.getPosition().getZ(), 1, 1);
            }
        }
        return canProjectileMove(a.getPosition().getX(), a.getPosition().getY(), b.getPosition().getX(),
                b.getPosition().getY(), a.getPosition().getZ(), 1, 1);
    }

    public static boolean canProjectileMove(int startX, int startY, int endX, int endY, int height,
                                            int xLength, int yLength) {
        int diffX = endX - startX;
        int diffY = endY - startY;
        // height %= 4;
        int max = Math.max(Math.abs(diffX), Math.abs(diffY));
        for (int ii = 0; ii < max; ii++) {
            int currentX = endX - diffX;
            int currentY = endY - diffY;
            for (int i = 0; i < xLength; i++) {
                for (int i2 = 0; i2 < yLength; i2++) {
                    if (diffX < 0 && diffY < 0) {
                        if ((RegionClipping.getClipping(currentX + i - 1, currentY + i2 - 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED | PROJECTILE_NORTH_EAST_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i - 1, currentY + i2, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i, currentY + i2 - 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX > 0 && diffY > 0) {
                        if ((RegionClipping.getClipping(currentX + i + 1, currentY + i2 + 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED | PROJECTILE_SOUTH_WEST_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i + 1, currentY + i2, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i, currentY + i2 + 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX < 0 && diffY > 0) {
                        if ((RegionClipping.getClipping(currentX + i - 1, currentY + i2 + 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED | PROJECTILE_SOUTH_EAST_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i - 1, currentY + i2, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i, currentY + i2 + 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX > 0 && diffY < 0) {
                        if ((RegionClipping.getClipping(currentX + i + 1, currentY + i2 - 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED | PROJECTILE_NORTH_BLOCKED
                                | PROJECTILE_NORTH_WEST_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i + 1, currentY + i2, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED)) != 0
                                || (RegionClipping.getClipping(currentX + i, currentY + i2 - 1, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX > 0 && diffY == 0) {
                        if ((RegionClipping.getClipping(currentX + i + 1, currentY + i2, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_WEST_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX < 0 && diffY == 0) {
                        if ((RegionClipping.getClipping(currentX + i - 1, currentY + i2, height)
                                & (UNLOADED_TILE | /* BLOCKED_TILE | */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_EAST_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX == 0 && diffY > 0) {
                        if ((RegionClipping.getClipping(currentX + i, currentY + i2 + 1, height)
                                & (UNLOADED_TILE | /*
                                    * BLOCKED_TILE |
                                    */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_SOUTH_BLOCKED)) != 0) {
                            return false;
                        }
                    } else if (diffX == 0 && diffY < 0) {
                        if ((RegionClipping.getClipping(currentX + i, currentY + i2 - 1, height)
                                & (UNLOADED_TILE | /*
                                    * BLOCKED_TILE |
                                    */UNKNOWN | PROJECTILE_TILE_BLOCKED
                                | PROJECTILE_NORTH_BLOCKED)) != 0) {
                            return false;
                        }
                    }
                }
            }
            if (diffX < 0) {
                diffX++;
            } else if (diffX > 0) {
                diffX--;
            }
            if (diffY < 0) {
                diffY++; // change
            } else if (diffY > 0) {
                diffY--;
            }
        }
        return true;
    }

    public final static boolean isInDiagonalBlock(Character attacked, Character attacker) {
        return attacked.getPosition().getX() - 1 == attacker.getPosition().getX()
                && attacked.getPosition().getY() + 1 == attacker.getPosition().getY()
                || attacker.getPosition().getX() - 1 == attacked.getPosition().getX()
                && attacker.getPosition().getY() + 1 == attacked.getPosition().getY()
                || attacked.getPosition().getX() + 1 == attacker.getPosition().getX()
                && attacked.getPosition().getY() - 1 == attacker.getPosition().getY()
                || attacker.getPosition().getX() + 1 == attacked.getPosition().getX()
                && attacker.getPosition().getY() - 1 == attacked.getPosition().getY()
                || attacked.getPosition().getX() + 1 == attacker.getPosition().getX()
                && attacked.getPosition().getY() + 1 == attacker.getPosition().getY()
                || attacker.getPosition().getX() + 1 == attacked.getPosition().getX()
                && attacker.getPosition().getY() + 1 == attacked.getPosition().getY();
    }

    public static final int PROJECTILE_NORTH_WEST_BLOCKED = 0x200;
    public static final int PROJECTILE_NORTH_BLOCKED = 0x400;
    public static final int PROJECTILE_NORTH_EAST_BLOCKED = 0x800;
    public static final int PROJECTILE_EAST_BLOCKED = 0x1000;
    public static final int PROJECTILE_SOUTH_EAST_BLOCKED = 0x2000;
    public static final int PROJECTILE_SOUTH_BLOCKED = 0x4000;
    public static final int PROJECTILE_SOUTH_WEST_BLOCKED = 0x8000;
    public static final int PROJECTILE_WEST_BLOCKED = 0x10000;
    public static final int PROJECTILE_TILE_BLOCKED = 0x20000;
    public static final int UNKNOWN = 0x80000;
    public static final int BLOCKED_TILE = 0x200000;
    public static final int UNLOADED_TILE = 0x1000000;
    public static final int OCEAN_TILE = 2097152;
}
