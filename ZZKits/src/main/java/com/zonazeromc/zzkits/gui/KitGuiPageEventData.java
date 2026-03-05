package com.zonazeromc.zzkits.gui;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Evento que el cliente envía al hacer click en un elemento de la UI.
 */
public class KitGuiPageEventData {

    static final String KEY_KIT = "Kit";
    static final String KEY_TYPE = "Type";

    public static final BuilderCodec<KitGuiPageEventData> CODEC = BuilderCodec
        .builder(KitGuiPageEventData.class, KitGuiPageEventData::new)
        .append(new KeyedCodec<>(KEY_KIT, Codec.STRING),
            (d, v) -> d.kit = v,
            d -> d.kit
        ).add()
        .append(new KeyedCodec<>(KEY_TYPE, Codec.STRING),
            (d, v) -> d.type = v,
            d -> d.type
        ).add()
        .build();

    private String kit;
    private String type;

    public KitGuiPageEventData() {}

    public String getKit() { return kit; }
    public String getType() { return type; }
}
