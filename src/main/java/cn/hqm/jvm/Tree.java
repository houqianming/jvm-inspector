package cn.hqm.jvm;

import java.util.List;


/**
 * 
 * @author Houqianming
 *
 * @param <E> 元素类型
 * @param <ID> ID的类型
 */
public interface Tree<E, ID> {
    String PATH_SEP_DOT = ".";


    List<Tree<E, ID>> getChildren();


    List<Tree<E, ID>> getTrees();


    List<E> getLeaves();


    boolean isLeaf();


    Tree<E, ID> getParent();


    void setParent(Tree<E, ID> parent);


    E getEntity();


    void setEntity(E entity);


    ID getId();


    Tree<E, ID> mkdirs(ID[] pathes);


    boolean addChild(ID[] parentPathes, Tree<E, ID> childTree, boolean mkdir);


    void addChild(Tree<E, ID> childTree);


    boolean removeChild(Tree<E, ID> childTree);


    void addEntity(E leafEntity, ID id);


    Tree<E, ID> removeEntity(E leafEntity);


    E removeEntityById(ID id);


    //List<String> getPath();
    //void setPath(List<String> path);
    Tree<E, ID> locate(ID[] pathes);


    /**
     * @param wholematch 
     *   true:  必须全匹配，只要有一个不匹配就返回null； 
     *   false：返回最长前缀匹配。完全不匹配才返回null
     */
    Tree<E, ID> locate(ID[] pathes, boolean wholematch);
}
