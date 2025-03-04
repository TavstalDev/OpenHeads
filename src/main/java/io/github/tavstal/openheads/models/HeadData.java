package io.github.tavstal.openheads.models;

public class HeadData {
    /**
     * The unique identifier for the head data.
     */
    public int Id;

    /**
     * Indicates if permission is required to access the head data.
     */
    public boolean RequirePermission;

    /**
     * The permission string required to access the head data.
     */
    public String Permission;

    /**
     * The price of the head data.
     */
    public Double Price;

    /**
     * The texture associated with the head data.
     */
    public String Texture;

    /**
     * Constructs a new HeadData object with the specified parameters.
     *
     * @param id The unique identifier for the head data.
     * @param requirePermission Indicates if permission is required to access the head data.
     * @param permission The permission string required to access the head data.
     * @param price The price of the head data.
     * @param texture The texture associated with the head data.
     */
    public HeadData(int id, boolean requirePermission, String permission, Double price, String texture) {
        Id = id;
        RequirePermission = requirePermission;
        Permission = permission;
        Price = price;
        Texture = texture;
    }
}
