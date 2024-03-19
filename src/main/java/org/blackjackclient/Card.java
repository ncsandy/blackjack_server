package org.blackjackclient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Card {

    @Getter @Setter
    Suite suite;

    @Getter @Setter
    Face face;

    @Getter @Setter
    int value;

    @Getter @Setter
    boolean ace;
    public Card() {
    }
    public Card(Suite suite, Face face, int value, boolean ace) {
        this.suite = suite;
        this.face = face;
        this.value = value;
        this.ace = ace;
    }
}
