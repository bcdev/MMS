package com.bc.fiduceo.reader.insitu.generic;

import org.esa.snap.core.datamodel.ProductData;
import org.junit.Test;

import static org.junit.Assert.*;


public class GenericVariableTest {

    @Test
    public void setProductData_byte() {
        GenericVariable var = new GenericVariable();
        var.setType("byte");
        assertEquals(ProductData.TYPE_INT8, var.getProductData());
    }

    @Test
    public void setProductData_short() {
        GenericVariable var = new GenericVariable();
        var.setType("short");
        assertEquals(ProductData.TYPE_INT16, var.getProductData());
    }

    @Test
    public void setProductData_int() {
        GenericVariable var = new GenericVariable();
        var.setType("int");
        assertEquals(ProductData.TYPE_INT32, var.getProductData());
    }

    @Test
    public void setProductData_long() {
        GenericVariable var = new GenericVariable();
        var.setType("long");
        assertEquals(ProductData.TYPE_INT64, var.getProductData());
    }
    @Test
    public void setProductData_float() {
        GenericVariable var = new GenericVariable();
        var.setType("float");
        assertEquals(ProductData.TYPE_FLOAT32, var.getProductData());
    }

    @Test
    public void setProductData_double() {
        GenericVariable var = new GenericVariable();
        var.setType("double");
        assertEquals(ProductData.TYPE_FLOAT64, var.getProductData());
    }

    @Test
    public void setProductData_string() {
        GenericVariable var = new GenericVariable();
        var.setType("string");
        assertEquals(ProductData.TYPE_ASCII, var.getProductData());
    }

    @Test
    public void setProductData_default() {
        GenericVariable var = new GenericVariable();

        try {
            var.setType("non_existent_type");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Unsupported type: non_existent_type", e.getMessage());
        }
    }
}