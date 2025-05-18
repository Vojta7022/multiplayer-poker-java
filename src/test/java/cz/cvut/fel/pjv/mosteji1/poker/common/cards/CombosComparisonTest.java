package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CombosComparisonTest {
    @Test
    void testThreeOfAKindBeatsTwoPairs() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertEquals(HandRanking.THREE_OF_A_KIND, combo1.getHandRanking());
        assertEquals(HandRanking.TWO_PAIRS, combo2.getHandRanking());
        assertTrue(combo1.compareTo(combo2) > 0, "Three of a kind should beat two Pairs");
    }

    @Test
    void testFlushBeatsStraight() {
        List<Card> hand1 = List.of(
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES)
        );

        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.HEARTS)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertEquals(HandRanking.FLUSH, combo1.getHandRanking());
        assertEquals(HandRanking.STRAIGHT, combo2.getHandRanking());
        assertTrue(combo1.compareTo(combo2) > 0, "Flush should beat Straight");
    }

    @Test
    void testRoyalFlushBeatsStraightFlush() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.CLUBS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.TWO, Suit.HEARTS)
        );

        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.THREE, Suit.DIAMONDS)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertEquals(HandRanking.ROYAL_FLUSH, combo1.getHandRanking());
        assertEquals(HandRanking.STRAIGHT_FLUSH, combo2.getHandRanking());
        assertTrue(combo1.compareTo(combo2) > 0, "Royal Flush should beat Straight Flush");
    }

    @Test
    void testFullHouseBeatsFlush() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.SPADES)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertEquals(HandRanking.FULL_HOUSE, combo1.getHandRanking());
        assertEquals(HandRanking.FLUSH, combo2.getHandRanking());
        assertTrue(combo1.compareTo(combo2) > 0, "Full House should beat Flush");
    }

    @Test
    void testStraightBeatsThreeOfAKind() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.TEN, Suit.SPADES),
                new Card(Rank.TWO, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertEquals(HandRanking.STRAIGHT, combo1.getHandRanking());
        assertEquals(HandRanking.THREE_OF_A_KIND, combo2.getHandRanking());
        assertTrue(combo1.compareTo(combo2) > 0, "Straight should beat Three of a Kind");
    }

    @Test
    void testTieWhenKickersEqual() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.KING, Suit.DIAMONDS)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertEquals(0, combo1.compareTo(combo2));
    }

    @Test
    void testKickerDecidesBetweenTwoPairs() {
        List<Card> hand1 = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.CLUBS)
        );

        List<Card> hand2 = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.JACK, Suit.DIAMONDS)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo1 = new CardCombo(hand1, community);
        CardCombo combo2 = new CardCombo(hand2, community);

        assertTrue(combo1.compareTo(combo2) > 0, "Hand1 should win with a higher kicker");
    }

    @Test
    void testTieWhenFlushesAreEqual () {
        List<Card> hand1 = List.of(
            new Card(Rank.FIVE, Suit.SPADES),
            new Card(Rank.KING, Suit.SPADES)
        );

        List<Card> hand2 = List.of(
            new Card(Rank.FOUR, Suit.CLUBS),
            new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> community = List.of(
            new Card(Rank.QUEEN, Suit.HEARTS),
            new Card(Rank.SEVEN, Suit.HEARTS),
            new Card(Rank.TEN, Suit.HEARTS),
            new Card(Rank.NINE, Suit.HEARTS),
            new Card(Rank.EIGHT, Suit.HEARTS)
        );

        assertEquals(0, new CardCombo(hand1, community).compareTo(new CardCombo(hand2, community)), "Two equal flushes should tie");
    }
}