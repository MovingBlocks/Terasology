package org.terasology.pathfinding.model;

import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.Chunk;

import java.util.*;

/**
 * @author synopia
 */
public class FloorFinder {
    private WorldProvider world;
    private List<Region> regions = new ArrayList<Region>();
    private Map<Region,Integer> count = new HashMap<Region, Integer>();
    private List<Sweep> sweeps = new ArrayList<Sweep>();
    private Map<WalkableBlock, Region> regionMap = new HashMap<WalkableBlock, Region>();
    private Map<WalkableBlock, Sweep> sweepMap = new HashMap<WalkableBlock, Sweep>();


    public FloorFinder(WorldProvider world) {
        this.world = world;
    }

    public Region region( WalkableBlock block ) {
        return regionMap.get(block);
    }
    public List<Region> regions() {
        return regions;
    }

    public void findFloors(HeightMap map) {
        findRegions(map);

        map.floors.clear();
        for (Region region : regions) {
            if( region.floor!=null ) {
                continue;
            }
            Floor floor = new Floor(map, map.floors.size());
            map.floors.add(floor);

            LinkedList<Region> stack = new LinkedList<Region>();
            stack.push(region);

            while (!stack.isEmpty()) {
                Collections.sort(stack, new Comparator<Region>() {
                    @Override
                    public int compare(Region o1, Region o2) {
                        return o1.id<o2.id ? -1 : o1.id>o2.id ? 1 : 0;
                    }
                });
                Region current = stack.poll();
                if( current.floor!=null ) {
                    continue;
                }
                if( !floor.overlap(current) ) {
                    floor.merge(current);

                    Set<Region> neighborRegions = current.getNeighborRegions();
                    for (Region neighborRegion : neighborRegions) {
                        if( neighborRegion.floor==null ) {
                            stack.add(neighborRegion);
                        }
                    }
                }
            }
        }

        for (Map.Entry<WalkableBlock, Region> entry : regionMap.entrySet()) {
            entry.getKey().floor = entry.getValue().floor;
        }

        for (int z = 0; z < HeightMap.SIZE_Z; z++) {
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                for (WalkableBlock block : map.getCell(x,z).blocks) {
                    Floor floor = block.floor;
                    boolean foundNeighborFloor = false;
                    for (WalkableBlock neighbor : block.neighbors) {
                        if( neighbor!=null && neighbor.floor!=floor ) {
                            foundNeighborFloor = true;
                            break;
                        }
                    }
                    if( foundNeighborFloor ) {
                        floor.setContour(block);
                    }
                }

            }
        }

        for (Floor floor : map.floors) {
            floor.findContour();
        }
    }

    void findRegions(HeightMap map) {
        Vector3i worldPos = map.worldPos;
        regions.clear();
        regionMap.clear();
        sweepMap.clear();

        for (int z = 0; z < HeightMap.SIZE_Z; z++) {
            count.clear();
            sweeps.clear();
            // find sweeps
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                int offset = x + z * HeightMap.SIZE_Z;
                HeightMapCell cell = map.cells[offset];

                findSweeps(cell);
            }
            // map sweeps to regions
            for (Sweep sweep : sweeps) {
                if( sweep.neighbor!=null && sweep.neighborCount== count.get(sweep.neighbor) ) {
                    sweep.region = sweep.neighbor;
                } else {
                    sweep.region = new Region(regions.size());
                    regions.add(sweep.region);
                }
            }
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                int offset = x + z * HeightMap.SIZE_Z;
                HeightMapCell cell = map.cells[offset];

                for (WalkableBlock block : cell.blocks) {
                    Sweep sweep = sweepMap.remove(block);
                    Region region = sweep.region;
                    regionMap.put(block, region);
                    region.setPassable(block.x() - worldPos.x, block.z() - worldPos.z);
                }
            }
        }
        findRegionNeighbors(map);
    }

    private void findSweeps(HeightMapCell cell) {
        for (WalkableBlock block : cell.blocks) {
            Sweep sweep;
            WalkableBlock leftNeighbor = block.neighbors[HeightMap.DIR_LEFT];
            if( leftNeighbor !=null ) {
                sweep = sweepMap.get(leftNeighbor);
            } else {
                sweep = new Sweep();
                sweeps.add(sweep);
            }
            WalkableBlock upNeighbor = block.neighbors[HeightMap.DIR_UP];
            if( upNeighbor !=null ) {
                Region neighborRegion = regionMap.get(upNeighbor);
                if( neighborRegion!=null) {
                    if( sweep.neighborCount==0 ) {
                        sweep.neighbor = neighborRegion;
                    }
                    if( sweep.neighbor==neighborRegion ) {
                        sweep.neighborCount++;
                        int c = count.containsKey(neighborRegion) ? count.get(neighborRegion) : 0;
                        c ++;
                        count.put(neighborRegion, c);
                    } else {
                        sweep.neighbor = null;
                    }
                }
            }
            sweepMap.put(block, sweep);
        }
    }

    private void findRegionNeighbors(HeightMap map) {
        for (int z = 0; z < HeightMap.SIZE_Z; z++) {
            for (int x = 0; x < HeightMap.SIZE_X; x++) {
                int offset = x + z * HeightMap.SIZE_Z;
                HeightMapCell cell = map.cells[offset];
                findRegionNeighbors(cell);
            }
        }
    }

    private void findRegionNeighbors(HeightMapCell cell) {
        for (WalkableBlock block : cell.blocks) {
            Region region = regionMap.get(block);
            if( region==null ) {
                continue;
            }

            for (WalkableBlock neighbor : block.neighbors) {
                Region neighborRegion = regionMap.get(neighbor);
                if( (neighbor==null) || (neighborRegion !=null && neighborRegion.id !=region.id) ) {
                    region.addNeighborBlock(block, neighbor, neighborRegion);
                }
            }
        }
    }

}
