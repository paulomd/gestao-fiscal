package br.com.entrevista.jsf.bean;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NavigationBeanTest {

    @Test
    void urlsAngular_comBaseNormalizada() {
        NavigationBean bean = new NavigationBean("http://localhost/");

        assertThat(bean.getUrlApuracao()).isEqualTo("http://localhost/apuracao-fiscal");
        assertThat(bean.getUrlHistorico()).isEqualTo("http://localhost/historico");
    }

    @Test
    void urlsAngular_semBase_retornaRotasRelativas() {
        NavigationBean bean = new NavigationBean("");

        assertThat(bean.getUrlApuracao()).isEqualTo("/apuracao-fiscal");
        assertThat(bean.getUrlHistorico()).isEqualTo("/historico");
    }

    @Test
    void urlsJsf_eLogout() {
        NavigationBean bean = new NavigationBean("http://demo");

        assertThat(bean.getUrlDashboard()).isEqualTo("/faces/dashboard.xhtml");
        assertThat(bean.getUrlAliquotas()).isEqualTo("/faces/aliquotas.xhtml");
        assertThat(bean.getUrlLogout()).isEqualTo("/logout");
    }
}
