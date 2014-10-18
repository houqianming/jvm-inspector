package cn.hqm.jvm;

import java.util.ArrayList;
import java.util.List;


/**
 * 
 * @author Houqianming
 *
 * @param <E>
 * @param <ID>
 */
public class TreeImpl<E, ID> implements Tree<E, ID> {
    public static final String PATH_SEP = PATH_SEP_DOT;
    private ID id; // not null
    //private String path;
    private E entity; //nullable
    private List<Tree<E, ID>> children = new ArrayList<Tree<E, ID>>();
    private Tree<E, ID> parent;
    private boolean isLeaf = true;


    public TreeImpl(E entity, ID id) {
        if (id == null)
            throw new java.lang.NullPointerException();

        this.entity = entity;
        this.id = id;
        //this.path = "".equals(this.id) ? "" : this.id + PATH_SEP;
    }


    /**===================================================================================
     * 遍历
     * =================================================================================*/

    @Override
    public List<Tree<E, ID>> getChildren() {
        return this.children;
    }


    @Override
    public List<Tree<E, ID>> getTrees() {
        List<Tree<E, ID>> res = new ArrayList<Tree<E, ID>>();
        for (Tree<E, ID> t : this.children)
            if (!t.isLeaf())
                res.add(t);
        return res;
    }


    @Override
    public List<E> getLeaves() {
        List<E> res = new ArrayList<E>();
        for (Tree<E, ID> t : this.children)
            if (t.isLeaf())
                res.add(t.getEntity());
        return res;
    }


    /**
     * @param path must be ended with pathSeparator: '.'
     */
    @Override
    public Tree<E, ID> locate(ID[] pathes) {
        return locate(pathes, true);
    }


    @Override
    public Tree<E, ID> locate(ID[] pathes, boolean wholematch) {
        Tree<E, ID> current = this;
        boolean found = false;
        for (ID path : pathes) {
            found = false;
            for (Tree<E, ID> tree : current.getChildren()) {
                if (tree.getId().equals(path)) {
                    current = tree;
                    found = true;
                    break;
                }
            }
            if (!found) {
                if (wholematch) {
                    return null;
                }
                else {
                    return current == this ? null : current;
                }
            }
        }
        return current;
    }


    /**===================================================================================
     * maintenance
     * =================================================================================*/

    @Override
    public Tree<E, ID> mkdirs(ID[] pathes) {
        Tree<E, ID> current = this;
        boolean found = false;
        for (ID path : pathes) {
            found = false;
            for (Tree<E, ID> tree : current.getChildren()) {
                if (tree.getId().equals(path)) {
                    current = tree;
                    found = true;
                    break;
                }
            }
            if (!found) {
                Tree<E, ID> newTree = new TreeImpl<E, ID>(null, path);
                current.addChild(newTree);
                current = newTree;
            }
        }
        return current;
    }


    @Override
    public boolean addChild(ID[] parentPathes, Tree<E, ID> childTree, boolean mkdir) {
        if (!mkdir)
            return addChild(parentPathes, childTree);

        Tree<E, ID> parent = mkdirs(parentPathes);
        parent.addChild(childTree);
        return true;
    }


    private boolean addChild(ID[] parentPathes, Tree<E, ID> childTree) {
        Tree<E, ID> parent = this.locate(parentPathes);
        if (parent == null)
            return false;
        parent.addChild(childTree);
        return true;
    }


    @Override
    public void addChild(Tree<E, ID> childTree) {
        childTree.setParent(this);
        this.children.add(childTree);
        this.isLeaf = false;
    }


    @Override
    public void addEntity(E leafEntity, ID name) {
        addChild(new TreeImpl<E, ID>(leafEntity, name));
    }


    @Override
    public boolean removeChild(Tree<E, ID> childTree) {
        boolean res = this.children.remove(childTree);
        this.isLeaf = this.children.isEmpty();
        return res;
    }


    @Override
    public Tree<E, ID> removeEntity(E leafEntity) {
        Tree<E, ID> firstMatchChild = null;
        for (Tree<E, ID> tree : this.children) {
            if (tree.getEntity() == null && leafEntity == null || tree.getEntity().equals(leafEntity)) {
                firstMatchChild = tree;
                break;
            }
        }
        if (firstMatchChild != null)
            this.removeChild(firstMatchChild);

        return firstMatchChild;
    }


    @Override
    public E removeEntityById(ID id) {
        Tree<E, ID> firstMatchChild = null;
        for (Tree<E, ID> tree : this.children) {
            if (tree.getId().equals(id)) {
                firstMatchChild = tree;
                break;
            }
        }
        if (firstMatchChild != null)
            this.removeChild(firstMatchChild);

        return firstMatchChild.getEntity();
    }


    /**===================================================================================
     * 覆盖
     * =================================================================================*/

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Tree))
            return false;
        Tree tree = (Tree) obj;
        return this.id.equals(tree.getId())
                && (this.entity == null && tree.getEntity() == null || this.entity.equals(tree.getEntity()));
    }


    @Override
    public int hashCode() {
        if (this.entity == null)
            return this.id.hashCode();
        return this.entity.hashCode();
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        toTextTree(sb, this, 0);
        return sb.toString();
    }


    //private static final String lineSep = System.getProperty("line.separator");
    private void toTextTree(StringBuilder sb, Tree<E, ID> tree, int deepth) {
        //sb.append("├─").append(this.id).append(lineSep);
        sb.append(super.toString());
    }


    /**===================================================================================
     * getter/setter
     * =================================================================================*/

    public E getEntity() {
        return this.entity;
    }


    public void setEntity(E entity) {
        this.entity = entity;
    }


    public Tree<E, ID> getParent() {
        return this.parent;
    }


    public void setParent(Tree<E, ID> parent) {
        this.parent = parent;
    }


    public ID getId() {
        return this.id;
    }


    public boolean isLeaf() {
        return this.isLeaf;
    }


    public static void main(String[] args) {
        System.out.println(System.getProperties());
        System.out.println("java.compiler=" + System.getProperty("java.compiler"));
    }
}
