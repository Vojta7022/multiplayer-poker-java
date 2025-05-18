package cz.cvut.fel.pjv.mosteji1.poker.common.cards;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CardComboTest {

    @Test
    void testHighCard() {
        List<Card> hand = List.of(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.FOUR, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.SIX, Suit.SPADES),
                new Card(Rank.EIGHT, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.CLUBS),
                new Card(Rank.ACE, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.HIGH_CARD, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Nejvyšší karta
    }

    @Test
    void testPairDetection() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.FIVE, Suit.DIAMONDS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.TEN, Suit.CLUBS),
                new Card(Rank.TWO, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.PAIR, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]);
    }

    @Test
    void testTwoPairsDetection() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.TWO_PAIRS, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Higher pair
        assertEquals(Rank.KING.ordinal(), combo.getKickers()[1]); // Lower pair
    }

    @Test
    void testThreeOfAKindDetection() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.THREE_OF_A_KIND, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Trips
    }

    @Test
    void testStraightDetection() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.THREE, Suit.SPADES),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.STRAIGHT, combo.getHandRanking());
        assertEquals(Rank.SEVEN.ordinal(), combo.getKickers()[0]); // Highest card in the straight
    }

    @Test
    void testFullHouseDetection() {
        List<Card> hand = List.of(
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.KING, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.FULL_HOUSE, combo.getHandRanking());
        assertEquals(Rank.KING.ordinal(), combo.getKickers()[0]); // Trips
        assertEquals(Rank.TEN.ordinal(), combo.getKickers()[1]);  // Pair
    }

    @Test
    void testFlushDetection() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS)
        );

        List<Card> community = List.of(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.NINE, Suit.SPADES),
                new Card(Rank.EIGHT, Suit.HEARTS),
                new Card(Rank.SEVEN, Suit.CLUBS)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.FLUSH, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Highest card in the flush
    }

    @Test
    void testStraightFlushDetection() {
        List<Card> hand = List.of(
                new Card(Rank.NINE, Suit.HEARTS),
                new Card(Rank.TEN, Suit.HEARTS)
        );

        List<Card> community = List.of(
                new Card(Rank.JACK, Suit.HEARTS),
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.TWO, Suit.CLUBS)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.STRAIGHT_FLUSH, combo.getHandRanking());
        assertEquals(Rank.KING.ordinal(), combo.getKickers()[0]); // Highest card in the straight flush
    }

    @Test
    void testRoyalFlushDetection() {
        List<Card> hand = List.of(
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.JACK, Suit.HEARTS)
        );

        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.ROYAL_FLUSH, combo.getHandRanking());
    }

    @Test
    void testFourOfAKindDetection() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.ACE, Suit.SPADES),
                new Card(Rank.TEN, Suit.DIAMONDS),
                new Card(Rank.KING, Suit.HEARTS),
                new Card(Rank.ACE, Suit.CLUBS),
                new Card(Rank.THREE, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.FOUR_OF_A_KIND, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Four of a kind
    }

    @Test
    void testStraightWithAceLow() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.TWO, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.THREE, Suit.SPADES),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.STRAIGHT, combo.getHandRanking());
        assertEquals(Rank.SEVEN.ordinal(), combo.getKickers()[0]); // Highest card in the straight
    }

    @Test
    void testStraightWithAceHigh() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.QUEEN, Suit.SPADES),
                new Card(Rank.JACK, Suit.DIAMONDS),
                new Card(Rank.TEN, Suit.HEARTS),
                new Card(Rank.NINE, Suit.CLUBS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.STRAIGHT, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Highest card in the straight
    }

    @Test
    void testStraightWithAceInMiddle() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.THREE, Suit.CLUBS)
        );

        List<Card> community = List.of(
                new Card(Rank.TWO, Suit.SPADES),
                new Card(Rank.FOUR, Suit.DIAMONDS),
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.SIX, Suit.CLUBS),
                new Card(Rank.SEVEN, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.STRAIGHT, combo.getHandRanking());
        assertEquals(Rank.SEVEN.ordinal(), combo.getKickers()[0]); // Highest card in the straight
    }

    @Test
    void testStraightWithAceInMiddleAndFlushWithKickersAndTwoPairsWithAce() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.THREE, Suit.HEARTS)
        );

        List<Card> community = List.of(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.FOUR, Suit.HEARTS),
                new Card(Rank.FIVE, Suit.HEARTS),
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.STRAIGHT_FLUSH, combo.getHandRanking());
        assertEquals(Rank.SIX.ordinal(), combo.getKickers()[0]); // Highest card in the straight flush
    }

    @Test
    void testStraightWithAceInMiddleAndFlushWithKickersAndTwoPairsWithAceAndKing() {
        List<Card> hand = List.of(
                new Card(Rank.ACE, Suit.HEARTS),
                new Card(Rank.KING, Suit.HEARTS)
        );

        List<Card> community = List.of(
                new Card(Rank.TWO, Suit.HEARTS),
                new Card(Rank.FOUR, Suit.HEARTS),
                new Card(Rank.THREE, Suit.HEARTS),
                new Card(Rank.SIX, Suit.HEARTS),
                new Card(Rank.EIGHT, Suit.SPADES)
        );

        CardCombo combo = new CardCombo(hand, community);

        assertEquals(HandRanking.FLUSH, combo.getHandRanking());
        assertEquals(Rank.ACE.ordinal(), combo.getKickers()[0]); // Highest card in the straight flush
    }
}