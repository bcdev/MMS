package com.bc.fiduceo.reader.slstr;

import org.junit.Test;

import static com.bc.fiduceo.reader.slstr.VariableType.Type.*;
import static org.junit.Assert.assertTrue;

public class TransformFactoryTest {

    @Test
    public void testGet() {
        final TransformFactory factory = new TransformFactory(100, 200, 18);

        Transform transform = factory.get(new VariableType(NADIR_1km));
        assertTrue(transform instanceof Nadir1kmTransform);

        transform = factory.get(new VariableType(NADIR_500m));
        assertTrue(transform instanceof Nadir500mTransform);

        transform = factory.get(new VariableType(OBLIQUE_1km));
        assertTrue(transform instanceof Oblique1kmTransform);

        transform = factory.get(new VariableType(OBLIQUE_500m));
        assertTrue(transform instanceof Oblique500mTransform);
    }
}
