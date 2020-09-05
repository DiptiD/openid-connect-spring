package com.gracenote.content.auth.persistence.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Represents the available end user authorities in database.
 *
 * @author deepak on 10/8/17.
 */

@Entity
public class Authority extends BaseEntity{

    private static final long serialVersionUID = 1L;

	@Id
    @NotNull
    @Size(min = 0, max = 50)
    @Column(name="role_name")
    private String rolename;
    
    @OneToMany(mappedBy = "authority",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<ApplicationAuthority> applicationAuthority;

    public String getName() {
        return rolename;
    }

    public void setName(String rolename) {
        this.rolename = rolename;
    }

    public Set<ApplicationAuthority> getApplicationAuthority() {
		return applicationAuthority;
	}

	public void setApplicationAuthority(Set<ApplicationAuthority> applicationAuthority) {
		this.applicationAuthority = applicationAuthority;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Authority authority = (Authority) o;

        if (!rolename.equals(authority.rolename)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return rolename.hashCode();
    }

    @Override
    public String toString() {
        return "Authority{" +
                "rolename='" + rolename + '\'' +
                '}';
    }

}
