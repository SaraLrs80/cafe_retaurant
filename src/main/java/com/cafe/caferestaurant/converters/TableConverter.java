package com.cafe.caferestaurant.converters;

import com.cafe.caferestaurant.dao.TableDAO;
import com.cafe.caferestaurant.entities.TableRestaurant;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("tableConverter")
public class TableConverter implements Converter<TableRestaurant> {

    private final TableDAO dao = new TableDAO();

    @Override
    public TableRestaurant getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            return null;
        }

        try {
            Long id = Long.parseLong(value);
            return dao.findById(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, TableRestaurant table) {
        if (table == null || table.getIdTable() == null) {
            return "";
        }
        return String.valueOf(table.getIdTable());
    }
}