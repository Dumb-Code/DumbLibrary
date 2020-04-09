package net.dumbcode.dumblibrary.client.gui;

public interface SelectListEntry extends GuiScrollboxEntry {
    /**
     * Gets the search string used for searching. <br>
     * Searching is done by checking if this term contains the searched term.
     * {@code selectListEntry.getSearch().contains("foo")} will mean any search terms with "foo" inside them at any point will be added to the found list
     *
     * @return the search term.
     */
    String getSearch();

}