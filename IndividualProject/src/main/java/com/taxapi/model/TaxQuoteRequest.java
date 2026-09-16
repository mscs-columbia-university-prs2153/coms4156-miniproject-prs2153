package com.taxapi.model;

/**
 * Represents a tax quote request.
 */
public final class TaxQuoteRequest {

    /** The state. */
    private String state;

    /** The item ID. */
    private String itemId;

    /** The price. */
    private Double price;

    /** The category. */
    private String category;

    /** Default constructor. */
    public TaxQuoteRequest() {
    }

    // /**
    //  * Fully-qualified constructor.
    //  * (added for testing)
    //  * 
    //  * @param state the state to set
    //  * @param itemId the itemId to set
    //  * @param price the price to set
    //  * @param category the category to set
    //  */
    // public TaxQuoteRequest(
    //     String state,
    //     String itemId,
    //     Double price,
    //     String category
    // ) {
    //     this.state = state;
    //     this.itemId = itemId;
    //     this.price = price;
    //     this.category = category;
    // }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state
     */
    public void setState(final String state) {
        this.state = state;
    }

    /**
     * Gets the item ID.
     *
     * @return the item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Sets the item ID.
     *
     * @param itemId the item ID
     */
    public void setItemId(final String itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets the price.
     *
     * @return the price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Sets the price.
     *
     * @param price the price
     */
    public void setPrice(final Double price) {
        this.price = price;
    }

    /**
     * Gets the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     *
     * @param category the category
     */
    public void setCategory(final String category) {
        this.category = category;
    }
}
