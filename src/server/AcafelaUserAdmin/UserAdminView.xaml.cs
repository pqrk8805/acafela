using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace AcafelaUserAdmin
{
    public partial class UserAdminView : UserControl
    {
        public UserAdminView()
        {
            Console.WriteLine("UserAdminView()");
            InitializeComponent();
        }

        private void OnRefreshButtonClick(object sender, RoutedEventArgs e)
        {
            UserAdminViewModel viewModel
                            = FindResource("viewModel") as UserAdminViewModel;
            viewModel.RefreshEntry();
        }

        private void OnDeleteButtonClick(object sender, RoutedEventArgs e)
        {
            UserAdminViewModel viewModel
                            = FindResource("viewModel") as UserAdminViewModel;
            DataGrid entry = FindName("entry") as DataGrid;
            viewModel.DeleteUser(entry.SelectedItems);
        }
    }
}
