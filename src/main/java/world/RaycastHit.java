package world;

public record RaycastHit(BlockPos blockPos, BlockPos adjacentPos, int blockId, float distance) {
}
