package com.github.tezvn.enchantic.impl.utils.inventory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An util class that help you build/arrange the inventory slots easily
 * <br><strong>Accept any contribution/modification but please keep the credit.</strong>
 *
 * @author TezVN
 * @version 1.0
 */
public class SlotBuilder {

    private final SlotIndex start = new SlotIndex(0, 0, 0, 0);
    private final SlotIndex end = new SlotIndex(53, 53, 5, 8);
    private final Map<Position, Integer> positions = new HashMap<>();
    private int row = 6;

    private int column = 9;

    public SlotBuilder() {
    }

    public int getTotalSlots(BuildMode mode) {
        return build(mode).size();
    }

    /**
     * Split the inventory slot with specific row and column.
     * @param startRow Start row
     * @param endRow End row
     * @param startColumn Start column
     * @param endColumn End columnd
     * @return Divided inventory slots.
     */
    public List<SlotIndex> split(int startRow, int endRow, int startColumn, int endColumn) {
        List<SlotIndex> list = new ArrayList<>();
        int j = -1;
        for (int i = 0; i < 54; i++) {
            int row = i / 9;
            int column = i - 9 * row;
            if ((row >= startRow && row <= endRow) && (column >= startColumn && column <= endColumn)) {
                SlotIndex index = new SlotIndex(++j, i, row, column);
                list.add(index);
            }
        }
        return list;
    }

    /**
     * Get the start position in inventory.
     *
     * @return Start slot.
     */
    public SlotIndex getStart() {
        SlotBuilder builder = new SlotBuilder();
        Arrays.stream(Position.values()).forEach(p -> builder.setBorder(p, 1));
        int bukkitSlot = start.get(SlotType.BUKKIT);
        bukkitSlot += getBorder(Position.LEFT);
        bukkitSlot += 9 * getBorder(Position.TOP);
        return new SlotIndex(0, bukkitSlot, 0, 0);
    }

    /**
     * Get the end position in inventory.
     *
     * @return End slot.
     */
    public SlotIndex getEnd() {
        int bukkitSlot = end.get(SlotType.BUKKIT);
        bukkitSlot -= getBorder(Position.RIGHT);
        bukkitSlot -= 9 * getBorder(Position.BOTTOM);
        return new SlotIndex(getRow() * getColumn(), bukkitSlot, getRow(), getColumn());
    }

    /**
     * Get row amount in inventory.
     */
    public int getRow() {
        return row;
    }

    /**
     * Get column amount in inventory.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get all border positions.
     *
     * @return List of border position
     */
    public Map<Position, Integer> getPositions() {
        return Collections.unmodifiableMap(this.positions);
    }

    /**
     * Set border in inventory
     *
     * @param position Position to set
     * @param value    Value to set
     */
    public SlotBuilder setBorder(Position position, int value) {
        int opposite = positions.getOrDefault(position.getOpposite(), 0);
        int left = Math.min(value, Math.max(0, position.getMax() - opposite - 1));
        this.positions.put(position, left);
        switch (position) {
            case LEFT:
            case RIGHT:
                this.column = Math.max(1, this.column - left);
                break;
            case TOP:
            case BOTTOM:
                this.row = Math.max(1, this.row - left);
                break;
        }
        return this;
    }

    /**
     * Remove border position if exist.
     *
     * @param position Position to remove.
     */
    public SlotBuilder removeBorder(Position position) {
        this.positions.remove(position);
        return this;
    }

    /**
     * Get the border position value.
     *
     * @param position Position to get.
     * @return Position value.
     */
    public int getBorder(Position position) {
        return this.positions.getOrDefault(position, 0);
    }

    /**
     * Get all the slots in that row.
     * <br>It might return an empty list if {@code row < 0 || row > 5}
     *
     * @param row Row to build
     * @return List of slots in this row.
     */
    public List<SlotIndex> buildByRow(int row) {
        List<SlotIndex> list = new ArrayList<>();
        if (row < 0 || row > this.row)
            return list;
        SlotIndex start = getStart();
        SlotIndex end = getEnd();
        int distance = getBorder(Position.LEFT) + getBorder(Position.RIGHT);
        for (int i = start.get(SlotType.ORDER); i < end.get(SlotType.ORDER); i++) {
            int currentRow = i / getColumn();
            int currentColumn = i - (currentRow*getColumn());
            int toAdd = i > 0 ? currentRow * distance : 0;
            int bukkitSlot = start.get(SlotType.BUKKIT) + i + toAdd;
            if (currentRow == row)
                list.add(new SlotIndex(i, bukkitSlot, currentRow, currentColumn));
        }
        return list;
    }

    /**
     * Get all the slots in that column.
     * <br>It might return an empty list if {@code column < 0 || column > 8}
     *
     * @param column Column to build
     * @return List of slots in this column.
     */
    public List<SlotIndex> buildByColumn(int column) {
        List<SlotIndex> list = new ArrayList<>();
        if (column < 0 || column > this.column)
            return list;
        SlotIndex start = getStart();
        SlotIndex end = getEnd();
        int distance = getBorder(Position.LEFT) + getBorder(Position.RIGHT);
        for (int i = start.get(SlotType.ORDER); i < end.get(SlotType.ORDER); i++) {
            int currentRow = i / getColumn();
            int currentColumn = i - (currentRow*getColumn());
            int toAdd = i > 0 ? currentRow * distance : 0;
            int bukkitSlot = start.get(SlotType.BUKKIT) + i + toAdd;
            if (currentColumn == column)
                list.add(new SlotIndex(i, bukkitSlot, currentRow, currentColumn));
        }
        return list;
    }

    /**
     * Get all the slots in specific axis range.
     *
     * @param axis  Axis to get
     * @param start Start axis point.
     * @param end   End axis point.
     * @return List of slots in that axis;
     */
    public List<SlotIndex> split(Axis axis, int start, int end) {
        List<SlotIndex> list = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            switch (axis) {
                case ROW:
                    list.addAll(buildByRow(i));
                    break;
                case COLUMN:
                    list.addAll(buildByColumn(i));
                    break;
            }
        }
        return list;
    }

    /**
     * Get all the slots in inventory.
     *
     * @param mode MOde to build
     * @return List of slot.
     */
    public List<SlotIndex> build(BuildMode mode) {
        List<SlotIndex> slots = toList();
        switch (mode) {
            case INSIDE:
                return slots;
            case OUTSIDE:
                List<Integer> inside = slots.stream().map(s -> s.get(SlotType.BUKKIT)).collect(Collectors.toList());
                slots.clear();
                int j = -1;
                for (int i = 0; i < 54; i++) {
                    if (inside.contains(i))
                        continue;
                    int row = i / 9;
                    int column = i - (9*row);
                    SlotIndex index = new SlotIndex(++j, i, row, column);
                    slots.add(index);
                }
                return slots;
            default:
                return Collections.emptyList();
        }
    }

    private List<SlotIndex> toList() {
        List<SlotIndex> list = new ArrayList<>();
        SlotIndex start = getStart();
        SlotIndex end = getEnd();
        int distance = getBorder(Position.LEFT) + getBorder(Position.RIGHT);
        for (int i = start.get(SlotType.ORDER); i < end.get(SlotType.ORDER); i++) {
            int currentRow = i / getColumn();
            int currentColumn = i - (currentRow*getColumn());
            int toAdd = i > 0 ? currentRow * distance : 0;
            int bukkitSlot = start.get(SlotType.BUKKIT) + i + toAdd;
            list.add(new SlotIndex(i, bukkitSlot, currentRow, currentColumn));
        }
        return list;
    }

    @Override
    public String toString() {
        return "InventorySlot{" +
                "start=" + getStart() +
                ", end=" + getEnd() +
                ", row=" + row +
                ", column=" + column +
                ", positions=" + positions +
                '}';
    }

    /**
     * Border position
     */
    public enum Position {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM;

        /**
         * Get the opposite position
         */
        public Position getOpposite() {
            switch (this) {
                case LEFT:
                    return RIGHT;
                case RIGHT:
                    return LEFT;
                case TOP:
                    return BOTTOM;
                case BOTTOM:
                    return TOP;
            }
            return null;
        }

        /**
         * Get max row/column.
         */
        public int getMax() {
            switch (this) {
                case LEFT:
                case RIGHT:
                    return 9;
                case TOP:
                case BOTTOM:
                    return 6;
            }
            return -1;
        }
    }

    public enum BuildMode {
        INSIDE,
        OUTSIDE
    }

    public enum Axis {
        ROW,
        COLUMN
    }

    public enum SlotType {
        ORDER,
        BUKKIT,
        ROW,
        COLUMN
    }

    public static class SlotIndex {

        private final int slot;

        private final int bukkitSlot;

        private final int row;

        private final int column;

        public SlotIndex(int slot, int bukkitSlot, int row, int column) {
            this.slot = slot;
            this.bukkitSlot = bukkitSlot;
            this.row = row;
            this.column = column;
        }

        /**
         * Get the slot by type
         *
         * @param type Type to get
         */
        public int get(SlotType type) {
            switch (type) {
                case ORDER:
                    return slot;
                case BUKKIT:
                    return bukkitSlot;
                case ROW:
                    return row;
                case COLUMN:
                    return column;
                default:
                    return -1;
            }
        }

        @Override
        public String toString() {
            return "SlotIndex{" +
                    "slot=" + slot +
                    ", bukkitSlot=" + bukkitSlot +
                    '}';
        }
    }

    public static class Crafting extends SlotBuilder {

        private int width;
        private int height;
        private int resultSlot;

        public Crafting() {
            this(3, 3);
        }

        public Crafting(int width, int height) {
            setWidth(width);
            setHeight(height);
            Arrays.stream(Position.values()).forEach(p -> {
                int value = 0;
                switch (p) {
                    case LEFT:
                    case TOP:
                        value = 1;
                        break;
                    case RIGHT:
                        value = 6;
                        break;
                    case BOTTOM:
                        value = 3;
                        break;
                }
                setBorder(p, value);
            });
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = Math.min(6, Math.max(2, width));
            setBorder(Position.RIGHT, getBorder(Position.RIGHT) + 2 - getWidth());
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = Math.min(3, Math.max(2, height));
        }

        public int getResultSlot() {
            return resultSlot;
        }

        public void setResultSlot(int resultSlot) {
            this.resultSlot = Math.max(0, Math.min(resultSlot, 53));
        }

    }

    public static class Boxed extends SlotBuilder {

        public Boxed() {
            Arrays.stream(Position.values()).forEach(p -> setBorder(p, 1));
        }
    }
}
