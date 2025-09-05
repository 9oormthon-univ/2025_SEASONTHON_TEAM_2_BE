package com.seasonthon.everflow.app.user.domain;

public enum BookshelfColor {
    GREEN, PINK, ORANGE, BLUE, YELLOW;

    private static final java.security.SecureRandom RND = new java.security.SecureRandom();

    public static BookshelfColor random() {
        BookshelfColor[] all = values();
        return all[RND.nextInt(all.length)];
    }

    public static BookshelfColor randomExcluding(java.util.Set<BookshelfColor> exclude) {
        java.util.List<BookshelfColor> cand = new java.util.ArrayList<>();
        for (BookshelfColor c : values()) if (exclude == null || !exclude.contains(c)) cand.add(c);
        // 전부 제외됐다면 아무거나
        if (cand.isEmpty()) return random();
        return cand.get(RND.nextInt(cand.size()));
    }
}
