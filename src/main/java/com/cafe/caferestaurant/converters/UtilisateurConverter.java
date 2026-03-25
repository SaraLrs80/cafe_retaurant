package com.cafe.caferestaurant.converters;

import com.cafe.caferestaurant.dao.UtilisateurDAO;
import com.cafe.caferestaurant.entities.Utilisateur;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

@FacesConverter("utilisateurConverter")
public class UtilisateurConverter implements Converter<Utilisateur> {

    private final UtilisateurDAO dao = new UtilisateurDAO();

    @Override
    public Utilisateur getAsObject(FacesContext ctx, UIComponent comp, String value) {
        if (value == null || value.isEmpty()) return null;
        return dao.findById(Long.parseLong(value));
    }

    @Override
    public String getAsString(FacesContext ctx, UIComponent comp, Utilisateur u) {
        if (u == null) return "";
        return String.valueOf(u.getIdUser());
    }
}