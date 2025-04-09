package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

public class CardCombo implements Comparable<CardCombo> {


    private final Card[] cards;
    private final int[] numberOfRanks = new int[Rank.values().length];
    private final int[] numberOfSuits = new int[Suit.values().length];
    private final boolean[][] containsSuitRank = new boolean[Suit.values().length][Rank.values().length];
    private HandRanking handRanking;
    private final int[] kickers = new int[5];

    public CardCombo(Card[] cards) {
        assert cards.length == 7;
        this.cards = cards;
        initializeRankArrays();
        determineHandRanking();
    }


    @Override
    public int compareTo(CardCombo other) {
        HandRanking otherHandRanking = other.getHandRanking();
        int[] otherKickers = other.getKickers();

        if (handRanking.ordinal() < otherHandRanking.ordinal()) return -1;
        if (handRanking.ordinal() > otherHandRanking.ordinal()) return 1;

        for (int kickerIndex = 0; kickerIndex < handRanking.numberOfKickers; kickerIndex++) {
            int myKicker = kickers[kickerIndex];
            int otherKicker = otherKickers[kickerIndex];
            if (myKicker < otherKicker) return -1;
            if (myKicker > otherKicker) return 1;
        }
        return 0;
    }

    public HandRanking getHandRanking() {
        return handRanking;
    }

    public int[] getKickers() {
        return kickers;
    }

    private void initializeRankArrays() {
        for (Card card : cards) {
            Rank myRank = card.rank();
            Suit mySuit = card.suit();
            numberOfRanks[myRank.ordinal()]++;
            numberOfSuits[mySuit.ordinal()]++;
            containsSuitRank[mySuit.ordinal()][myRank.ordinal()] = true;
        }
    }

    /* EACH AND EVERY POSSIBLE POKER HAND EVALUATION */

    private void determineHandRanking() {
        if (containsRoyalFlush()) {handRanking = HandRanking.ROYAL_FLUSH; return;}
        if (containsStraightFlush()) {handRanking = HandRanking.STRAIGHT_FLUSH; return;}
        if (containsFourOfAKind()) {handRanking = HandRanking.FOUR_OF_A_KIND; return;}
        if (containsFullHouse()) {handRanking = HandRanking.FULL_HOUSE; return;}
        if (containsFlush()) {handRanking = HandRanking.FLUSH; return;}
        if (containsStraight()) {handRanking = HandRanking.STRAIGHT; return;}
        if (containsThreeOfAKind()) {handRanking = HandRanking.THREE_OF_A_KIND; return;}        // TODO: zkonzultuj pár, dva páry a vysokou kartu s panem gpt
        if (containsTwoPairs()) {handRanking = HandRanking.TWO_PAIRS; return;}
        if (containsPair()) {handRanking = HandRanking.PAIR; return;}

        setHighCard();
        handRanking = HandRanking.HIGH_CARD;
    }

    private boolean containsRoyalFlush() {
        for (Suit suit : Suit.values()) {
            boolean hasAll = true;
            for (Rank rank : Rank.royalFlushCards) {
                if (!containsSuitRank[suit.ordinal()][rank.ordinal()]) {
                    hasAll = false;
                    break;
                }
            }
            if (hasAll) {
                return true;
            }
        }
        return false;
    }

    private boolean containsStraightFlush() {
        for (Suit suit : Suit.values()) {
            for (int rank = Rank.KING.ordinal() /* we know there's no royal flush */; Rank.SIX.ordinal() <= rank; rank--) {
                boolean hasAll = true;
                for (int dRank = 0; dRank < 5; dRank++) {
                    if (!containsSuitRank[suit.ordinal()][rank - dRank]) {
                        hasAll = false;
                        break;
                    }
                }
                if (hasAll) {
                    kickers[0] = rank;
                    return true;
                }
            }
            boolean hasWheel = true;
            for (Rank rank : Rank.theWheel) {
                if (!containsSuitRank[suit.ordinal()][rank.ordinal()]) {
                    hasWheel = false;
                    break;
                }
            }
            if (hasWheel) {
                kickers[0] = Rank.FIVE.ordinal();
                return true;
            }
        }
        return false;
    }

    private boolean containsFourOfAKind() {
        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] == 4) {
                kickers[0] = rank;
                for (int kicker = Rank.ACE.ordinal() ; kicker >= Rank.TWO.ordinal() ; kicker--) {
                    if (kicker != rank && 0 < numberOfRanks[kicker]) {
                        kickers[1] = kicker;
                        break;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean containsFullHouse() {
        int threeOfAKind = -1;
        int pair = -1;

        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] == 3) {
                threeOfAKind = rank;
                break;
            }
        }
        if (threeOfAKind == -1) return false;

        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] >= 2 && rank != threeOfAKind) {
                pair = rank;
                break;
            }
        }
        if (pair == -1) return false;

        kickers[0] = threeOfAKind;
        kickers[1] = pair;
        return true;
    }

    private boolean containsFlush() {
        for (Suit suit : Suit.values()) {
            if (numberOfSuits[suit.ordinal()] >= 5) {
                int kickerIndex = 0;
                for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
                    if (containsSuitRank[suit.ordinal()][rank]) {
                        kickers[kickerIndex] = rank;
                        kickerIndex++;
                    }
                    if (kickerIndex == 5) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean containsStraight() {
        for (int rank = Rank.ACE.ordinal(); Rank.SIX.ordinal() <= rank; rank--) {
            boolean hasAll = true;
            for (int dRank = 0; dRank < 5; dRank++) {
                if (numberOfRanks[rank - dRank] == 0) {
                    hasAll = false;
                    break;
                }
            }
            if (hasAll) {
                kickers[0] = rank;
                return true;
            }
        }
        boolean hasWheel = true;
        for (Rank rank : Rank.theWheel) {
            if (numberOfRanks[rank.ordinal()] == 0) {
                hasWheel = false;
                break;
            }
        }
        if (hasWheel) {
            kickers[0] = Rank.FIVE.ordinal();
            return true;
        }
        return false;
    }

    private boolean containsThreeOfAKind() {
        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] == 3) {
                kickers[0] = rank;
                int kickerIndex = 1;
                for (int kicker = Rank.ACE.ordinal() ; kicker >= Rank.TWO.ordinal() ; kicker--) {
                    if (kicker != rank && 0 < numberOfRanks[kicker]) {
                        kickers[kickerIndex] = kicker;
                        kickerIndex++;
                    }
                    if (kickerIndex == 3) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean containsTwoPairs() {
        int pair1 = -1;
        int pair2 = -1;

        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] == 2) {
                pair1 = rank;
                break;
            }
        }
        if (pair1 == -1) return false;

        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] >= 2 && rank != pair1) {
                pair2 = rank;
                break;
            }
        }
        if (pair2 == -1) return false;

        kickers[0] = pair1;
        kickers[1] = pair2;

        for (int kicker = Rank.ACE.ordinal() ; kicker >= Rank.TWO.ordinal() ; kicker--) {
            if (kicker != pair1 && kicker != pair2 && 0 < numberOfRanks[kicker]) {
                kickers[2] = kicker;
                break;
            }
        }
        return true;
    }

    private boolean containsPair() {
        int pair = -1;
        for (int rank = Rank.ACE.ordinal() ; rank >= Rank.TWO.ordinal() ; rank--) {
            if (numberOfRanks[rank] == 2) {
                pair = rank;
                break;
            }
        }
        if (pair == -1) return false;

        int kickerIndex = 1;
        for (int kicker = Rank.ACE.ordinal() ; kicker >= Rank.TWO.ordinal() ; kicker--) {
            if (kicker != pair && 0 < numberOfRanks[kicker]) {
                kickers[kickerIndex] = kicker;
                kickerIndex++;
            }
            if (kickerIndex == 4) {
                break;
            }
        }
        return true;
    }

    private void setHighCard() {
        int kickerIndex = 0;
        for (int kicker = Rank.ACE.ordinal() ; kicker >= Rank.TWO.ordinal() ; kicker--) {
            if (0 < numberOfRanks[kicker]) {
                kickers[kickerIndex] = kicker;
                kickerIndex++;
            }
            if (kickerIndex == 5) {
                break;
            }
        }
    }

}
