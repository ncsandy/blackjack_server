import org.blackjackserver.BlackJackPlayer
import org.blackjackserver.Game

import spock.lang.Specification

class GameSpec extends Specification {

    def socket = Mock(Socket)
    def player = Mock(BlackJackPlayer)
    def game = Mock(Game)

    def setup() {
        socket.getInputStream() >> new ByteArrayInputStream(new byte[0])
        socket.getOutputStream() >> new ByteArrayOutputStream()
        player = new BlackJackPlayer(socket)
        game = new Game()
    }

    def "test calculator method for win outcome"() {
        given:

        player.money = 100
        player.bet = 10

        when:
        game.calculator("win", player)

        then:
        player.money == 120
    }

    def "test calculator method for lose outcome"() {
        given:

        player.setMoney(100)
        player.setBet(10)

        when:
        game.calculator("lose", player)

        then:
        player.money == 90
    }

    def "test calculator method for blackjack outcome"() {
        given:

        player.setMoney(100)
        player.setBet(10)

        when:
        game.calculator("blackjack", player)

        then:
        player.money == 125
    }

    def "test calculator method for invalid outcome"() {
        given:

        player.setMoney(100)
        player.setBet(10)

        when:
        game.calculator("invalid", player)

        then:
        thrown IllegalArgumentException
    }
}