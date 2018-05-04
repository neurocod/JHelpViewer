package jhelpviewer;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

class DocItem implements Comparable<DocItem>, Externalizable
{
	private static final long serialVersionUID = 9191069736556627657L;
	public enum Type
	{
		Article,
		Package,
		Enum,
		Interface,
		Class,
		Exeption,
		Error,
		Method,
		Constructor,
		Variable,
		Annotation,
		Unknown,
	}
	
	static public boolean isContainerJavaType(Type t)
	{
		Type[]types = {	Type.Enum, Type.Interface, Type.Class, Type.Exeption, Type.Error};
		for(Type t2: types)
		{
			if(t2==t)
			{
				return true;
			}
		}
		return false;
	}

	public DocItem(String name, Type type, String fileName)
	{
		this.fileName = fileName;
		this.name = name;
		this.lowercaseName = name.toLowerCase();
		this.type = type;
	}
	
	public boolean sameSignatureAndFile(DocItem other)
	{
		if(other==null)
			return false;
		return nameWithSignature.equals(other.nameWithSignature)
		&& fileName.equals(other.fileName)
		&& classOrPackage.equals(other.classOrPackage)
		;
	}

	@Override
	public int compareTo(DocItem other)
	{
		int ret = this.name.compareTo(other.name);
		if(0==ret)
		{
			ret = this.nameWithSignature.compareTo(other.nameWithSignature);
		}
		return ret;
	}

	public String getClassOrPackage()
	{
		return classOrPackage;
	}

	public String getDescription()
	{
		return description;
	}

	public String getFileName()
	{
		return fileName;
	}

	public String getLowercaseName()
	{
		return lowercaseName;
	}

	public String getName()
	{
		return name;
	}
	public String getNameWithSignature()
	{
		return nameWithSignature;
	}
	public Type getType()
	{
		return type;
	}
	/**
	 * @param classOrPackage the classOrPackage to set
	 */
	public void setClassOrPackage(String classOrPackage)
	{
		this.classOrPackage = classOrPackage;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	/**
	 * @param nameWithSignature the nameWithSignature to set
	 */
	public void setNameWithSignature(String nameWithSignature)
	{
		this.nameWithSignature = nameWithSignature;
	}
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("name=");
		builder.append(name);

		builder.append("\nnameWithSignature=");
		builder.append(nameWithSignature);
		
		builder.append("\ndescription=");
		builder.append(description);
		
		builder.append("\nfileName=");
		builder.append(fileName);
		
		builder.append("\ntype=");
		builder.append(type);
		
		builder.append("\n");
		
		return builder.toString();
	}
	
	/**
	 * public constructor with no parameters - only for serialization with Externalizable
	 * */
	public DocItem()
	{
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		name = (String)in.readObject();
		lowercaseName = name.toLowerCase();
		nameWithSignature = (String)in.readObject();
		description = (String)in.readObject();
		fileName = (String)in.readObject();
		classOrPackage = (String)in.readObject();
		type = (Type)in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeObject(name);
		out.writeObject(nameWithSignature);
		out.writeObject(description);
		out.writeObject(fileName);
		out.writeObject(classOrPackage);
		out.writeObject(type);
	}
	
	private String name;
	private String lowercaseName;
	private String nameWithSignature;
	private String description;
	private String fileName;
	private String classOrPackage;
	private Type type;
}