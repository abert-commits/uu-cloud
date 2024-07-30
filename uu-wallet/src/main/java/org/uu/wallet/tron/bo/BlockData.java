
package org.uu.wallet.tron.bo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Setter
@Getter
public class BlockData implements Serializable {


    private static final long serialVersionUID = -393826214266762999L;


    private String txTrieRoot;


    private String witnessAddress;


    private String parentHash;


    private int number;


    private Long timestamp;


    private Long version;
}