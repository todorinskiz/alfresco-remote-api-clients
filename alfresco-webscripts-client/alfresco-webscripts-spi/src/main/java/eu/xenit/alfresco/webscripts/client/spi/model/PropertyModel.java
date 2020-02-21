package eu.xenit.alfresco.webscripts.client.spi.model;

import eu.xenit.testing.ditto.api.model.QName;
import java.io.Serializable;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PropertyModel {

    private QName qName;
    private QName type;
    private String dataType;
    private Function<Serializable, String> deserializer;
    private boolean isContent;
    private boolean isNodeRef;
    private boolean isResidual;
    private boolean isMultiple;

}
