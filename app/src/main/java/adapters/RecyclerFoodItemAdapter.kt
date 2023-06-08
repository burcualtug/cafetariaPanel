package adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafetariapanel.R
import com.squareup.picasso.Picasso
import datamodels.MenuItem
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class RecyclerFoodItemAdapter(
    var context: Context,
    private var itemList: ArrayList<MenuItem>,
    private val loadDefaultImage: Int,
    val listener: OnItemClickListener
) :
    RecyclerView.Adapter<RecyclerFoodItemAdapter.ItemListViewHolder>(), Filterable {

    private var fullItemList = ArrayList<MenuItem>(itemList)

    interface OnItemClickListener {
        fun onItemClick(item: MenuItem)
        fun onPlusBtnClick(item: MenuItem)
        fun onMinusBtnClick(item: MenuItem)
        fun deleteItem(item: MenuItem)
    }

    class ItemListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImageIV: ImageView = itemView.findViewById(R.id.item_image)
        val itemNameTV: TextView = itemView.findViewById(R.id.item_name)
        val itemPriceTV: TextView = itemView.findViewById(R.id.item_price)
        //val itemStarsTV: TextView = itemView.findViewById(R.id.item_stars)
        val itemShortDesc: TextView = itemView.findViewById(R.id.item_short_desc)
        val itemDelete: ImageView = itemView.findViewById(R.id.delete_item)
        /*val itemQuantityTV: TextView = itemView.findViewById(R.id.item_quantity_tv)
        val itemQuantityIncreaseIV: ImageView =
            itemView.findViewById(R.id.increase_item_quantity_iv)
        val itemQuantityDecreaseIV: ImageView =
            itemView.findViewById(R.id.decrease_item_quantity_iv)*/
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemListViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_menu_item, parent, false)
        fullItemList = ArrayList<MenuItem>(itemList)
        return ItemListViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemListViewHolder, position: Int) {
        val currentItem = itemList[position]

        if (loadDefaultImage == 1) holder.itemImageIV.setImageResource(R.drawable.default_item_image)
        else Picasso.get().load(currentItem.imageUrl).into(holder.itemImageIV)

        holder.itemNameTV.text = currentItem.itemName
        holder.itemPriceTV.text = "$${currentItem.itemPrice}"
        //holder.itemStarsTV.text = currentItem.itemStars.toString()
        holder.itemShortDesc.text = currentItem.itemShortDesc
        Log.d("IMAGEURL",currentItem.imageUrl)

        /*var imageURL: String=currentItem.imageUrl.toString().substring(2)
        imageURL = imageURL.substring(0,(imageURL.length-1))
        Log.d("IMAGEURL2",currentItem.imageUrl)*/
        //val url: String = "https://firebasestorage.googleapis.com/v0/b/cafetariaapp.appspot.com/o/itemImages%2F0040009140378551494505087461418921131744.jpeg?alt=media&token=af36119a-07ac-4781-884c-e46da85176a6"

        Picasso.get().load(currentItem.imageUrl).into(holder.itemImageIV)

        /*Glide.with(context)
            .load(currentItem.imageUrl)
            .into(holder.itemImageIV)*/

        /*try { // loading ID Card
            Picasso.get().load(currentItem.imageUrl).into(holder.itemImageIV)
        } catch (e: Exception) {
            e.printStackTrace()
        }*/

        holder.itemDelete.setOnClickListener {
            listener.deleteItem(itemList[position])
        }
        /*holder.itemQuantityTV.text = currentItem.quantity.toString()

        holder.itemQuantityIncreaseIV.setOnClickListener {
            val n = currentItem.quantity
            holder.itemQuantityTV.text = (n+1).toString()

            listener.onPlusBtnClick(currentItem)
        }

        holder.itemQuantityDecreaseIV.setOnClickListener {
            val n = currentItem.quantity
            if (n == 0) return@setOnClickListener
            holder.itemQuantityTV.text = (n-1).toString()

            listener.onMinusBtnClick(currentItem)
        }*/

        holder.itemView.setOnClickListener {
            listener.onItemClick(currentItem)
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun filterList(filteredList: ArrayList<MenuItem>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return searchFilter;
    }

    private val searchFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<MenuItem>()
            if (constraint!!.isEmpty()) {
                filteredList.addAll(fullItemList)
            } else {
                val filterPattern = constraint.toString().toLowerCase(Locale.ROOT).trim()

                for (item in fullItemList) {
                    if (item.itemName.toLowerCase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            itemList.clear()
            itemList.addAll(results!!.values as ArrayList<MenuItem>)
            notifyDataSetChanged()
        }

    }
}