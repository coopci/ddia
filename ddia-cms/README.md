# DDIA cms
提供cms功能。

每一个内容被称为一个item。 每个item可以有任意多个属性。

有一些属性是"强制的": create_time, appid(表示出数据哪一个应用  例如 gift), type(表示在appid指明的范围内的作用。 例如 album, article)。 

item可以作为容器包含其他的item。 容器中的子item有顺序。任何一个item都可以属于多个容器。 容器可以包含容器。

item可以有名字也可以没有名字。在有名字的情况下，相同owner_id的item的名字不能重复。

global_name 是一个全局查找item的机制。用来实现"B端发布所有人可读的内容"功能。
