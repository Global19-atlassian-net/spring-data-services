package org.springframework.data.services.repository.jpa;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.services.Handler;
import org.springframework.util.ReflectionUtils;

/**
 * @author Jon Brisbin <jon@jbrisbin.com>
 */
public class JpaEntityMetadata {

  private Class<?> targetType;
  private Map<String, Attribute> embeddedAttributes = new HashMap<String, Attribute>();
  private Map<String, Field> fields = new HashMap<String, Field>();
  private Map<String, Attribute> linkedAttributes = new HashMap<String, Attribute>();
  private final Attribute idAttribute;
  private final Attribute versionAttribute;

  @SuppressWarnings({"unchecked"})
  public JpaEntityMetadata(EntityType entityType, JpaRepositoryMetadata repositoryMetadata) {
    targetType = entityType.getJavaType();

    Attribute idAttribute = null;
    Attribute versionAttribute = null;
    for (Attribute attr : (Set<Attribute>) entityType.getAttributes()) {
      String name = attr.getName();
      Field f = ReflectionUtils.findField(targetType, attr.getName());
      ReflectionUtils.makeAccessible(f);
      fields.put(name, f);

      if (attr instanceof SingularAttribute) {
        SingularAttribute sattr = (SingularAttribute) attr;
        if (sattr.isId()) {
          idAttribute = attr;
        } else if (sattr.isVersion()) {
          versionAttribute = attr;
        } else if (null != repositoryMetadata.repositoryFor(attr.getJavaType())) {
          linkedAttributes.put(name, attr);
        } else {
          embeddedAttributes.put(name, attr);
        }
      } else if (attr instanceof PluralAttribute) {
        PluralAttribute pattr = (PluralAttribute) attr;
        if (pattr.getElementType() instanceof EntityType
            && null != repositoryMetadata.repositoryFor(pattr.getElementType().getJavaType())) {
          linkedAttributes.put(name, attr);
        } else {
          embeddedAttributes.put(name, attr);
        }
      }
    }

    this.idAttribute = idAttribute;
    this.versionAttribute = versionAttribute;
  }

  public Map<String, Attribute> embeddedAttributes() {
    return Collections.unmodifiableMap(embeddedAttributes);
  }

  public Map<String, Attribute> linkedAttributes() {
    return Collections.unmodifiableMap(linkedAttributes);
  }

  public Attribute idAttribute() {
    return idAttribute;
  }

  public Attribute versionAttribute() {
    return versionAttribute;
  }

  public Object id(Object target) {
    return get(idAttribute.getName(), target);
  }

  public Object version(Object target) {
    return get(versionAttribute.getName(), target);
  }

  public <V> V doWithEmbedded(Handler<Attribute, V> handler) {
    if (null == handler) {
      return null;
    }
    V v = null;
    for (Attribute attr : embeddedAttributes.values()) {
      v = handler.handle(attr);
    }
    return v;
  }

  public <V> V doWithLinked(String name, Handler<Attribute, V> handler) {
    if (null == handler) {
      return null;
    }
    V v = null;
    Attribute attr = linkedAttributes.get(name);
    if (null != attr) {
      v = handler.handle(attr);
    }
    return v;
  }

  public <V> V doWithLinked(Handler<Attribute, V> handler) {
    if (null == handler) {
      return null;
    }
    V v = null;
    for (Attribute attr : linkedAttributes.values()) {
      v = handler.handle(attr);
    }
    return v;
  }

  public Object get(String name, Object target) {
    try {
      return fields.get(name).get(target);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }

}
